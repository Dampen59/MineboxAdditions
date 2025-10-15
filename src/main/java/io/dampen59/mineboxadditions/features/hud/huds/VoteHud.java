package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.minecraft.text.Text;

public class VoteHud extends Hud {
    public VoteHud() {
        super(
                () -> HudsConfig.vote,
                s -> HudsConfig.vote = s,
                () -> HudPositions.vote.x,
                x -> HudPositions.vote.x = x,
                () -> HudPositions.vote.y,
                y -> HudPositions.vote.y = y);
    }

    @Override
    public StackElement init() {
        TextElement text = new TextElement(Text.of("5 votes"));

        HStackElement hstack = new HStackElement()
                .add(new SpacerElement(4))
                .add(new VStackElement().add(new SpacerElement(1), text))
                .add(new SpacerElement(4));
        addNamedElement("text", text);

        return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
    }
}