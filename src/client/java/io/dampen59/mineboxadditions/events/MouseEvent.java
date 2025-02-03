package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class MouseEvent {

    private int EVENT_START_TIME = 13500;
    private int EVENT_STOP_TIME = 20000;
    private boolean isShopOpen = false;

    private State modState = null;

    public MouseEvent(State prmModState) {
        this.modState = prmModState;
        HudRenderCallback.EVENT.register(this::onRenderHud);
        onTick();
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (modState.getConnectedToMinebox()) {
                if (client.world != null) {
                    long currentWorldTicks = client.world.getTimeOfDay() % 24000;

                    if (currentWorldTicks >= EVENT_START_TIME && currentWorldTicks <= EVENT_STOP_TIME) {
                        isShopOpen = true;
                        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                        if (config.shopsAlertsSettings.getMouseAlerts == true) {
                            if (this.modState.getMouseAlertSent() == false) {
                                Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.mouse.open.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.mouse.open.content").getString());
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setMouseAlertSent(true);
                            }
                        }
                    } else {
                        isShopOpen = false;
                        if (this.modState.getMouseAlertSent()) {
                            this.modState.setMouseAlertSent(false);
                        }
                        if (this.modState.getMouseCurrentItemOffer() != null) {
                            this.modState.setMouseCurrentItemOffer(null);
                        }
                    }
                }
            }
        });
    }

    private void onRenderHud(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (MinecraftClient.getInstance().options.hudHidden) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (this.modState.getMouseCurrentItemOffer() != null) {
            drawContext.drawText(client.textRenderer, this.modState.getMouseCurrentItemOffer(), 5, 40, 0xFFFFFF, true);
        } else if (isShopOpen) {
            drawContext.drawText(client.textRenderer, Text.translatable("mineboxadditions.strings.toasts.shop.mouse.open.title").getString(), 5, 40, 0xFFFFFF, true);
        }
    }

}
