package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class BuckstarEvent {
    private int EVENT_START_TIME = 10; // 10 to prevent 0-tick when changing world
    private int EVENT_STOP_TIME = 6000;
    private boolean isShopOpen = false;

    private State modState = null;

    public BuckstarEvent(State prmModState) {
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
                        if (config.shopsAlertsSettings.getBuckstarAlerts == true) {
                            if (this.modState.getBuckstarAlertSent() == false) {
                                Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.open.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.open.content").getString());
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setBuckstarAlertSent(true);
                            }
                        }
                    } else {
                        isShopOpen = false;
                        if (this.modState.getBuckstarAlertSent()) {
                            this.modState.setBuckstarAlertSent(false);
                        }
                        if (this.modState.getBuckstarCurrentItemOffer() != null) {
                            this.modState.setBuckstarCurrentItemOffer(null);
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

        if (this.modState.getBuckstarCurrentItemOffer() != null) {
            drawContext.drawText(client.textRenderer, this.modState.getBuckstarCurrentItemOffer(), 5, 40, 0xFFFFFF, true);
        } else if (isShopOpen) {
            drawContext.drawText(client.textRenderer, Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.open.title").getString(), 5, 40, 0xFFFFFF, true);
        }
    }
}
