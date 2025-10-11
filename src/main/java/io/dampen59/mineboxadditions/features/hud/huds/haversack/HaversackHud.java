package io.dampen59.mineboxadditions.features.hud.huds.haversack;

import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextureElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HaversackHud {
    public static class RateHud extends Hud {
        public RateHud() {
            super(
                    () -> HudsConfig.haversack.rate,
                    s -> HudsConfig.haversack.rate = s,
                    () -> HudPositions.haversackRate.x,
                    x -> HudPositions.haversackRate.x = x,
                    () -> HudPositions.haversackRate.y,
                    y -> HudPositions.haversackRate.y = y);
        }

        @Override
        public StackElement init() {
            Identifier texture = Identifier.of("mineboxadditions", "textures/icons/haversack.png");
            TextElement text = new TextElement(Text.of("0.0/s"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new TextureElement(texture, 10, 10))
                    .add(new SpacerElement(4))
                    .add(new VStackElement()
                            .add(new SpacerElement(1))
                            .add(new HStackElement().add(new TextElement(Text.of("Fill Rate:")), new SpacerElement(3), text)))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        public static void render(DrawContext context, double rate) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_RATE);
            if (!hud.getState()) return;
            String text = String.format("%.2f/s", rate);
            hud.getNamedElement("text", TextElement.class).setText(Text.of(text));
            hud.draw(context);
        }
    }

    public static class FullHud extends Hud {
        public FullHud() {
            super(
                    () -> HudsConfig.haversack.full,
                    s -> HudsConfig.haversack.full = s,
                    () -> HudPositions.haversackFull.x,
                    x -> HudPositions.haversackFull.x = x,
                    () -> HudPositions.haversackFull.y,
                    y -> HudPositions.haversackFull.y = y);
        }

        @Override
        public StackElement init() {
            Identifier texture = Identifier.of("mineboxadditions", "textures/icons/haversack.png");
            TextElement text = new TextElement(Text.of("00:00:00"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new TextureElement(texture, 10, 10))
                    .add(new SpacerElement(4))
                    .add(new VStackElement()
                            .add(new SpacerElement(1))
                            .add(new HStackElement().add(new TextElement(Text.of("Full In:")), new SpacerElement(3), text)))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        public static void render(DrawContext context, String time) {
            Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_FULL);
            if (!hud.getState()) return;
            hud.getNamedElement("text", TextElement.class).setText(Text.of(time));
            hud.draw(context);
        }
    }
}
