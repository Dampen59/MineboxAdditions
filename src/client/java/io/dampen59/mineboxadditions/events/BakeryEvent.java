package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class BakeryEvent {

    private int EVENT_START_TIME = 6000;
    private int EVENT_STOP_TIME = 12000;

    private State modState = null;

    public BakeryEvent(State prmModState) {
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
                        if (config.shopsAlertsSettings.getBakeryAlerts == true) {
                            if (this.modState.getBakeryAlertSent() == false) {
                                Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.bakery.open.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.bakery.open.content").getString());
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setBakeryAlertSent(true);
                            }
                        }
                    } else {
                        if (this.modState.getBakeryAlertSent()) {
                            this.modState.setBakeryAlertSent(false);
                        }
                        if (this.modState.getBakeryCurrentItemOffer() != null) {
                            this.modState.setBakeryCurrentItemOffer(null);
                        }
                    }
                }
            }
        });
    }

}
