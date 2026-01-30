package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SkyEvent {
    public SkyEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            int moonPhase = client.world.getMoonPhase();
            MineboxAdditions.INSTANCE.state.setCurrentMoonPhase(moonPhase);
        });
    }
}
