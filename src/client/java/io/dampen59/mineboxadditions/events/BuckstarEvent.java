package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvents;

public class BuckstarEvent {
    private int EVENT_START_TIME = 10; // 10 to prevent 0-tick when changing world
    private int EVENT_STOP_TIME = 6000;

    private State modState = null;

    public BuckstarEvent(State prmModState) {
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
                        if (config.shopsAlertsSettings.getBuckstarAlerts == true) {
                            if (this.modState.getBuckstarAlertSent() == false) {
                                Utils.showToastNotification("Buckstar","Need a caffeine shot ? Buckstar just opened !");
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setBuckstarAlertSent(true);
                            }
                        }
                    } else {
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
}
