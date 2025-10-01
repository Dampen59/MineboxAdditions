package io.dampen59.mineboxadditions.features.hud.haversack;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HaversackHud {
    public static class RateHud extends Hud {
        public RateHud() {
            super(
                    () -> MineboxAdditionConfig.get().displaySettings.displayHaversackFillRate,
                    s -> MineboxAdditionConfig.get().displaySettings.displayHaversackFillRate = s,
                    () -> MineboxAdditionConfig.get().haverSackFillRateX,
                    () -> MineboxAdditionConfig.get().haverSackFillRateY,
                    x -> MineboxAdditionConfig.get().haverSackFillRateX = x,
                    y -> MineboxAdditionConfig.get().haverSackFillRateY = y,
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
                    () -> MineboxAdditionConfig.get().displaySettings.displayHaversackFullIn,
                    s -> MineboxAdditionConfig.get().displaySettings.displayHaversackFullIn = s,
                    () -> MineboxAdditionConfig.get().haversackFullInX,
                    () -> MineboxAdditionConfig.get().haversackFullInY,
                    x -> MineboxAdditionConfig.get().haversackFullInX = x,
                    y -> MineboxAdditionConfig.get().haversackFullInY = y,
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
