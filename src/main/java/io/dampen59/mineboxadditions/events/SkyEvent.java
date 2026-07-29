package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.clock.WorldClocks;

public class SkyEvent {
    public SkyEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;
            int moonPhase = (int)(client.level.clockManager().getTotalTicks(client.level.registryAccess().getOrThrow(WorldClocks.OVERWORLD)) / 24000L % 8L);
            MineboxAdditions.INSTANCE.state.setCurrentMoonPhase(moonPhase);
        });
    }
}
