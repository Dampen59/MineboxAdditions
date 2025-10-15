package io.dampen59.mineboxadditions.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.utils.SocketManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ShinyTracker {
    private static Map<String, Boolean> shinyUuids = new HashMap<>();
    private static String lastShinyUuid = "";
    private static String lastShinyKey = "";

    public static int getShinyCount() {
        return shinyUuids.size();
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ShinyTracker::tick);
        ClientCommandRegistrationCallback.EVENT.register(ShinyTracker::command);
        SocketManager.getSocket().on("S2CShinyEvent", ShinyTracker::event);
    }

    private static void event(Object[] args) {
        if (Config.shinyNotify == Config.ShinyNotify.OFF) return;

        String playerName = (String) args[0];
        String shinyKey = (String) args[1];
        String shinyUuid = (String) args[2];

        shinyUuids.put(shinyUuid, true);

        Text player = Text.literal(playerName)
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
        Text shiny = Text.translatable(shinyKey)
                .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));
        Text message = Text.translatable("mineboxadditions.shiny.notify.message", player, shiny)
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false)
                        .withClickEvent(new ClickEvent.RunCommand("/tpa " + playerName)));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(message, false);
            client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private static void tick(MinecraftClient client) {
        if (Config.shinyNotify == Config.ShinyNotify.OFF) return;
        if (client.player == null || client.world == null) return;

        Vec3d position = client.player.getPos();
        Box searchBox = Box.from(position).expand(5, 5, 5);

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof DisplayEntity.TextDisplayEntity display)) continue;
            if (!entity.getBoundingBox().intersects(searchBox)) continue;

            for (Text sibling : display.getText().getSiblings()) {
                if (!(sibling.getContent() instanceof TranslatableTextContent content)) continue;
                if (!content.getKey().startsWith("mbx.bestiary")) continue;

                TextColor color = sibling.getStyle().getColor();
                if (color == null || !color.getHexCode().equals("#FEFE00")) continue;

                if (shinyUuids.containsKey(display.getUuidAsString())) continue;
                lastShinyUuid = display.getUuidAsString();
                lastShinyKey = content.getKey();
                shinyUuids.put(lastShinyUuid, false);

                if (Config.shinyNotify == Config.ShinyNotify.MANUAL) {
                    Text shiny = Text.translatable(lastShinyKey)
                            .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));
                    Text message = Text.translatable("mineboxadditions.shiny.found", shiny)
                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                                    .withClickEvent(new ClickEvent.RunCommand("/mbaSendShinyAlert"))
                            );
                    client.player.sendMessage(message, false);
                } else if (Config.shinyNotify == Config.ShinyNotify.AUTO) {
                    client.player.networkHandler.sendPacket(new CommandExecutionC2SPacket("mbaSendShinyAlert"));
                }
            }
        }
    }

    private static void command(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registry) {
        var command = ClientCommandManager.literal("mbaSendShinyAlert");
        dispatcher.register(command.executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.player == null || !shinyUuids.containsKey(lastShinyUuid))
                return Command.SINGLE_SUCCESS;

            Text message;
            if (!shinyExists()) {
                message = Text.translatable("mineboxadditions.shiny.notify.not_exists")
                        .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(false));
            } else if (shinyUuids.get(lastShinyUuid) == false) {
                shinyUuids.replace(lastShinyUuid, true);

                Text text = Text.translatable("mineboxadditions.shiny.found.message", Text.translatable(lastShinyKey));
                client.player.networkHandler.sendChatMessage(text.getString());
                SocketManager.getSocket().emit("C2SShinyEvent", lastShinyUuid, lastShinyKey);

                message = Text.translatable("mineboxadditions.shiny.notify")
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false));
            } else {
                message = Text.translatable("mineboxadditions.shiny.notify.error")
                        .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(false));
            }

            if (Config.shinyNotify == Config.ShinyNotify.MANUAL)
                client.player.sendMessage(message, false);

            return Command.SINGLE_SUCCESS;
        }));
    }

    public static boolean shinyExists() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof DisplayEntity.TextDisplayEntity display)) continue;
            if (lastShinyUuid.equals(display.getUuidAsString())) return true;
        }
        return false;
    }
}