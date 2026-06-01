package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SkyEvent {
    public SkyEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            int moonPhase = (int)(client.level.getGameTime() / 24000L % 8L);
            MineboxAdditions.INSTANCE.state.setCurrentMoonPhase(moonPhase);
        });
    }
}
