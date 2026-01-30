package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.*;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class WeatherHud {
    public static class FullMoonHud extends Hud {
        public FullMoonHud() {
            super(
                    () -> HudsConfig.fullmoon,
                    s -> HudsConfig.fullmoon = s,
                    () -> HudPositions.fullMoon.x,
                    x -> HudPositions.fullMoon.x = x,
                    () -> HudPositions.fullMoon.y,
                    y -> HudPositions.fullMoon.y = y);
        }

        @Override
        public StackElement init() {
            Identifier texture = Identifier.of("mineboxadditions", "textures/icons/full_moon.png");

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(2))
                    .add(new TextureElement(texture, 10, 10))
                    .add(new SpacerElement(2));

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }
    }

    public static class RainHud extends Hud {
        public RainHud() {
            super(
                    () -> HudsConfig.rain,
                    s -> HudsConfig.rain = s,
                    () -> HudPositions.rain.x,
                    x -> HudPositions.rain.x = x,
                    () -> HudPositions.rain.y,
                    y -> HudPositions.rain.y = y);

            ClientTickEvents.END_CLIENT_TICK.register(this::update);
        }

        @Override
        public StackElement init() {
            Identifier texture = Identifier.of("mineboxadditions", "textures/icons/rain.png");
            TextElement text = new TextElement(Text.of("00:00:00"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new TextureElement(texture, 10, 10))
                    .add(new SpacerElement(4))
                    .add(new VStackElement().add(new SpacerElement(1), text))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        private void update(MinecraftClient client) {
            if (client.world == null) return;
            String text = (client.world.isRaining()
                    ? "Now!"
                    : formatNextEventCountdown(MineboxAdditions.INSTANCE.state.getWeatherState().getRainTimestamps()));
            getNamedElement("text", TextElement.class).setText(Text.of(text));
        }
    }

    public static class StormHud extends Hud {
        public StormHud() {
            super(
                    () -> HudsConfig.storm,
                    s -> HudsConfig.storm = s,
                    () -> HudPositions.storm.x,
                    x -> HudPositions.storm.x = x,
                    () -> HudPositions.storm.y,
                    y -> HudPositions.storm.y = y);

            ClientTickEvents.END_CLIENT_TICK.register(this::update);
        }

        @Override
        public StackElement init() {
            Identifier texture = Identifier.of("mineboxadditions", "textures/icons/storm.png");
            TextElement text = new TextElement(Text.of("00:00:00"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new TextureElement(texture, 10, 10))
                    .add(new SpacerElement(4))
                    .add(new VStackElement().add(new SpacerElement(1), text))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        private void update(MinecraftClient client) {
            if (client.world == null) return;
            String text = (client.world.isThundering()
                    ? "Now!"
                    : formatNextEventCountdown(MineboxAdditions.INSTANCE.state.getWeatherState().getStormTimestamps()));
            getNamedElement("text", TextElement.class).setText(Text.of(text));
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
