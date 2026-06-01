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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IslandHud extends Hud {
    public IslandHud() {
        super(
                () -> HudsConfig.island,
                s -> HudsConfig.island = s,
                () -> HudPositions.island.x,
                x -> HudPositions.island.x = x,
                () -> HudPositions.island.y,
                y -> HudPositions.island.y = y);
    }

    @Override
    public StackElement init() {
        Identifier texture = Identifier.fromNamespaceAndPath("mineboxadditions", "textures/icons/island.png");
        TextElement text = new TextElement(Component.literal("Island"));

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