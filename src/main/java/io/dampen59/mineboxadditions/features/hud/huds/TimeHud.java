package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextureElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TimeHud extends Hud {
    public TimeHud() {
        super(
                () -> HudsConfig.time,
                s -> HudsConfig.time = s,
                () -> HudPositions.time.x,
                x -> HudPositions.time.x = x,
                () -> HudPositions.time.y,
                y -> HudPositions.time.y = y);
    }

    @Override
    public StackElement init() {
        Identifier texture = Identifier.of("mineboxadditions", "textures/icons/time.png");
        TextElement text = new TextElement(Text.of("00:00"));

        HStackElement hstack = new HStackElement()
                .add(new SpacerElement(4))
                .add(new TextureElement(texture, 10, 10))
                .add(new SpacerElement(4))
                .add(new VStackElement().add(new SpacerElement(1), text))
                .add(new SpacerElement(4));
        addNamedElement("text", text);

        return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
    }
}