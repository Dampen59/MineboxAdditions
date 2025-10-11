package io.dampen59.mineboxadditions.features.hud.haversack;

import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HaversackHud {
    public static class RateHud extends Hud {
        public RateHud() {
            super(
                    () -> HudsConfig.haversack.rate,
                    s -> HudsConfig.haversack.rate = s,
                    () -> HudPositions.haversackRate.x,
                    () -> HudPositions.haversackRate.y,
                    x -> HudPositions.haversackRate.x = x,
                    y -> HudPositions.haversackRate.y = y,
                    "haversack", Text.of("Fill Rate: 0.0/s"));
        }

        public static void render(DrawContext context, double rate) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_RATE);
            if (!hud.getState()) return;
            String rateText = String.format("Fill Rate: %.2f/s", rate);
            hud.setText(Text.of(rateText));
            hud.draw(context);
        }
    }

    public static class FullHud extends Hud {
        public FullHud() {
            super(
                    () -> HudsConfig.haversack.full,
                    s -> HudsConfig.haversack.full = s,
                    () -> HudPositions.haversackFull.x,
                    () -> HudPositions.haversackFull.y,
                    x -> HudPositions.haversackFull.x = x,
                    y -> HudPositions.haversackFull.y = y,
                    "haversack", Text.of("Full In: 00:00:00"));
        }

        public static void render(DrawContext context, String time) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_FULL);
            if (!hud.getState()) return;
            String timeText = "Full In: " + time;
            hud.setText(Text.of(timeText));
            hud.draw(context);
        }
    }
}
