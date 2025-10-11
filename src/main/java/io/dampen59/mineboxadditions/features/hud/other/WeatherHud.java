package io.dampen59.mineboxadditions.features.hud.other;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class WeatherHud {
    public static class RainHud extends Hud {
        public RainHud() {
            super(
                    () -> HudsConfig.rain,
                    s -> HudsConfig.rain = s,
                    () -> HudPositions.rain.x,
                    () -> HudPositions.rain.y,
                    x -> HudPositions.rain.x = x,
                    y -> HudPositions.rain.y = y,
                    "rain", Text.of("00:00:00"));
        }

        public static void render(DrawContext context, MinecraftClient client) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.RAIN);
            if (!hud.getState()) return;
            String text = (client.world.isRaining()
                    ? "Now!"
                    : formatNextEventCountdown(MineboxAdditions.INSTANCE.state.getWeatherState().getRainTimestamps()));
            hud.setText(Text.of(text));
            hud.draw(context);
        }
    }

    public static class StormHud extends Hud {
        public StormHud() {
            super(
                    () -> HudsConfig.storm,
                    s -> HudsConfig.storm = s,
                    () -> HudPositions.storm.x,
                    () -> HudPositions.storm.y,
                    x -> HudPositions.storm.x = x,
                    y -> HudPositions.storm.y = y,
                    "storm", Text.of("00:00:00"));
        }

        public static void render(DrawContext context, MinecraftClient client) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.STORM);
            if (!hud.getState()) return;
            String text = (client.world.isThundering()
                    ? "Now!"
                    : formatNextEventCountdown(MineboxAdditions.INSTANCE.state.getWeatherState().getStormTimestamps()));
            hud.setText(Text.of(text));
            hud.draw(context);
        }
    }

    private static String formatNextEventCountdown(List<Integer> timestamps) {
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
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
