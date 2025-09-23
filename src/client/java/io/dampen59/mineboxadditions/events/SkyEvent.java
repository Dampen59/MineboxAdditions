package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.state.HUDState;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class SkyEvent {
    private final State modState;

    public SkyEvent(State prmModState) {
        this.modState = prmModState;
        onTick();
        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                int moonPhase = client.world.getMoonPhase();

                if (this.modState.getCurrentMoonPhase() != 0) {
                    // TODO : if config.enableFullMoonAlerts
                }

                this.modState.setCurrentMoonPhase(moonPhase);
            }
        });
    }

    private boolean isFullMoon() {
        return this.modState.getCurrentMoonPhase() == 0;
    }

    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        HUDState hudState = MineboxAdditionsClient.INSTANCE.modState.getHUDState();

        if (isFullMoon() && config.displaySettings.displayFullMoon) {
            hudState.getHud(Hud.Type.FULL_MOON).draw(context);
        }

        if (config.displaySettings.displayNextRain) {
            String text = (client.world.isRaining()
                    ? "Now!"
                    : formatNextEventCountdown(modState.getWeatherState().getRainTimestamps()));
            Hud hud = hudState.getHud(Hud.Type.RAIN);
            hud.setText(Text.of(text));
            hud.draw(context);
        }

        if (config.displaySettings.displayNextStorm) {
            String text = (client.world.isThundering()
                    ? "Now!"
                    : formatNextEventCountdown(modState.getWeatherState().getStormTimestamps()));
            Hud hud = hudState.getHud(Hud.Type.STORM);
            hud.setText(Text.of(text));
            hud.draw(context);
        }
    }

    private String formatNextEventCountdown(List<Integer> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) return "Unknown";

        List<Integer> snapshot = snapshotList(timestamps);
        if (snapshot.isEmpty()) return "Unknown";

        int now = (int) (System.currentTimeMillis() / 1000L);
        Integer next = null;
        for (Integer ts : snapshot) {
            if (ts != null && ts > now && (next == null || ts < next)) {
                next = ts;
            }
        }
        if (next == null) return "Unknown";

        int secondsLeft = next - now;
        int hours = secondsLeft / 3600;
        int minutes = (secondsLeft % 3600) / 60;
        int seconds = secondsLeft % 60;
        return String.format("in %02d:%02d:%02d", hours, minutes, seconds);
    }

    private static List<Integer> snapshotList(List<Integer> src) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return new ArrayList<>(src);
            } catch (ConcurrentModificationException ignored) { }
        }
        List<Integer> fallback = new ArrayList<>();
        try {
            for (Integer i : src) fallback.add(i);
        } catch (Exception ignored) { }
        return fallback;
    }
}
