package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class MouseEvent {

    private int EVENT_START_TIME = 13500;
    private int EVENT_STOP_TIME = 20000;

    private State modState = null;

    public MouseEvent(State prmModState) {
        this.modState = prmModState;
        onTick();
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (modState.getConnectedToMinebox()) {
                if (client.world != null) {
                    long currentWorldTicks = client.world.getTimeOfDay() % 24000;

                    if (currentWorldTicks >= EVENT_START_TIME && currentWorldTicks <= EVENT_STOP_TIME) {
                        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                        if (config.shopsAlertsSettings.getMouseAlerts == true) {
                            if (this.modState.getMouseAlertSent() == false) {
                                Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.mouse.open.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.mouse.open.content").getString());
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setMouseAlertSent(true);
                            }
                        }
                    } else {
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

}
