package io.dampen59.mineboxadditions.events;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.dampen59.mineboxadditions.state.State;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

public class ShinyEvent {
    private State modState = null;
    public String shinyUuid = null;
    public String mobNameKey = null;

    public ShinyEvent(State prmModState) {
        this.modState = prmModState;
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        onTick();
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mbaSendShinyAlert").executes(context -> {

            Text message = null;

            if (!this.modState.getMbxShiniesUuids().containsKey(shinyUuid)) return Command.SINGLE_SUCCESS;

            if (!this.modState.getMbxShiniesUuids().get(shinyUuid)) {
                this.modState.getSocket().emit("C2SShinyEvent", this.shinyUuid, this.mobNameKey);
                this.modState.getMbxShiniesUuids().replace(shinyUuid, true);
                message = Text.literal("Other MineboxAdditions users have been alerted. Thank you !")
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true));
            } else {
                message = Text.literal("You've already alerted other MineboxAdditions users !")
                        .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true));
            }

            MinecraftClient.getInstance().player.sendMessage(message, false);

            return Command.SINGLE_SUCCESS;
        }));
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            var player = client.player;
            var playerPos = player.getPos();

            Box searchBox = new Box(
                    playerPos.x - 5, playerPos.y - 5, playerPos.z - 5,
                    playerPos.x + 5, playerPos.y + 5, playerPos.z + 5
            );


            for (Entity entity : client.world.getEntities()) {

                if (!(entity instanceof DisplayEntity.TextDisplayEntity textDisplay)) continue;
                if (!entity.getBoundingBox().intersects(searchBox)) continue;

                Text textComponent = textDisplay.getText();

                for (Text sib : textComponent.getSiblings()) {
                    if (sib.getContent() instanceof TranslatableTextContent sibTr) {
                        String translationKey = sibTr.getKey();
                        if (!translationKey.startsWith("mbx.bestiary")) continue;
                        Style mobNameStyle = sib.getStyle();
                        if (isMobShiny(mobNameStyle)) {
                            this.shinyUuid = textDisplay.getUuidAsString();
                            if (this.modState.getMbxShiniesUuids().containsKey(this.shinyUuid)) continue;
                            this.modState.addShinyUuid(this.shinyUuid);
                            this.mobNameKey = translationKey;
                            String mobName = sib.getString();
                            sendShinyAlert(client, mobName);
                        }
                    }
                }
            }
        });
    }

    private void sendShinyAlert(MinecraftClient client, String prmMobName) {
        if (client.player == null) return;

        Text baseMessage = Text.literal("You just found a Shiny ")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true));

        Text mobText = Text.literal( "[" + prmMobName + "]")
                .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));

        Text endMessage = Text.literal(" ! Click on this message to alert other MineboxAdditions users.")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mbaSendShinyAlert")));


        Text message = baseMessage.copy().append(mobText).append(endMessage);

        client.player.sendMessage(message, false);
    }

    private boolean isMobShiny(Style style) {
        return style.getColor() != null && style.getColor().getHexCode().equals("#FEFE00");
    }
}