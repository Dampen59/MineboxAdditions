package io.dampen59.mineboxadditions.features;
import net.minecraft.network.chat.contents.TranslatableContents;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.utils.SocketManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

        Component player = Component.literal(playerName)
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(true));
        Component shiny = Component.translatable("mineboxadditions.shiny", Component.translatable(shinyKey))
                .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));
        Component message = Component.translatable("mineboxadditions.shiny.notify.message", player, shiny)
                .setStyle(Style.EMPTY
                        .withColor(0x578EC7)
                        .withClickEvent(new ClickEvent.RunCommand("/tpa " + playerName)));

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(message);
            client.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private static void tick(Minecraft client) {
        if (Config.shinyNotify == Config.ShinyNotify.OFF) return;
        if (client.player == null || client.level == null) return;

        Vec3 position = client.player.position();
        AABB searchBox = new AABB(position.x-5, position.y-5, position.z-5, position.x+5, position.y+5, position.z+5);

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Display.TextDisplay display)) continue;
            if (!entity.getBoundingBox().intersects(searchBox)) continue;

            for (Component sibling : display.getText().getSiblings()) {
                if (!(sibling.getContents() instanceof TranslatableContents content)) continue;
                if (!content.getKey().startsWith("mbx.bestiary")) continue;

                TextColor color = sibling.getStyle().getColor();
                if (color == null || color.getValue() != 0xFEFE00) continue;

                if (shinyUuids.containsKey(display.getStringUUID())) continue;
                lastShinyUuid = display.getStringUUID();
                lastShinyKey = content.getKey();
                shinyUuids.put(lastShinyUuid, false);

                if (Config.shinyNotify == Config.ShinyNotify.MANUAL) {
                    Component shiny = Component.translatable("mineboxadditions.shiny", Component.translatable(lastShinyKey))
                            .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));
                    Component message = Component.translatable("mineboxadditions.shiny.found", shiny)
                            .setStyle(Style.EMPTY
                                    .withColor(0x578EC7)
                                    .withClickEvent(new ClickEvent.RunCommand("/mbaSendShinyAlert")));
                    client.player.sendSystemMessage(message);
                } else if (Config.shinyNotify == Config.ShinyNotify.AUTO) {
                    client.player.connection.send(new ServerboundChatCommandPacket("mbaSendShinyAlert"));
                }
            }
        }
    }

    private static void command(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registry) {
        var command = ClientCommands.literal("mbaSendShinyAlert");
        dispatcher.register(command.executes(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null || !shinyUuids.containsKey(lastShinyUuid))
                return Command.SINGLE_SUCCESS;

            Component message;
            if (!shinyExists()) {
                message = Component.translatable("mineboxadditions.shiny.notify.not_exists")
                        .setStyle(Style.EMPTY.withColor(0xFF2034));
            } else if (shinyUuids.get(lastShinyUuid) == false) {
                shinyUuids.replace(lastShinyUuid, true);

                Component text = Component.translatable("mineboxadditions.shiny.found.message",
                        Component.translatable("mineboxadditions.shiny", Component.translatable(lastShinyKey)));
                client.player.connection.sendChat(text.getString());
                SocketManager.getSocket().emit("C2SShinyEvent", lastShinyUuid, lastShinyKey);

                message = Component.translatable("mineboxadditions.shiny.notify")
                        .setStyle(Style.EMPTY.withColor(0x00FD72));
            } else {
                message = Component.translatable("mineboxadditions.shiny.notify.error")
                        .setStyle(Style.EMPTY.withColor(0xFF2034));
            }

            if (Config.shinyNotify == Config.ShinyNotify.MANUAL)
                client.player.sendSystemMessage(message);

            return Command.SINGLE_SUCCESS;
        }));
    }

    public static boolean shinyExists() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return false;

        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof Display.TextDisplay display)) continue;
            if (lastShinyUuid.equals(display.getStringUUID())) return true;
        }
        return false;
    }
}