package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvents;

public class CocktailEvent {
    private int EVENT_START_TIME = 12000;
    private int EVENT_STOP_TIME = 13500;

    private State modState = null;

    public CocktailEvent(State prmModState) {
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
                        if (config.shopsAlertsSettings.getCocktailAlerts == true) {
                            if (this.modState.getCocktailAlertSent() == false) {
                                Utils.showToastNotification("Cocktail","Need a refreshment ? The cocktail bar is open !");
                                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                                this.modState.setCocktailAlertSent(true);
                            }
                        }
                    } else {
                        if (this.modState.getCocktailAlertSent()) {
                            this.modState.setCocktailAlertSent(false);
                        }
                        if (this.modState.getCocktailCurrentItemOffer() != null) {
                            this.modState.setCocktailCurrentItemOffer(null);
                        }
                    }
                }
            }
        });
    }
}
