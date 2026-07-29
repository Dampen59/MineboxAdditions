package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.categories.MineboxDefaultHuds;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class IslandHud extends Hud {
    public IslandHud() {
        super(
                () -> MineboxDefaultHuds.island,
                s -> MineboxDefaultHuds.island = s,
                () -> HudPositions.island.x,
                x -> HudPositions.island.x = x,
                () -> HudPositions.island.y,
                y -> HudPositions.island.y = y);

        ClientTickEvents.END_CLIENT_TICK.register(this::update);
    }

    private void update(Minecraft client) {
        Component island = MineboxAdditions.INSTANCE.state.getBossbarIsland();
        if (island != null) {
            getNamedElement("text", TextElement.class).setValue(island);
        }
    }

    @Override
    public boolean shouldRender() {
        return MineboxAdditions.INSTANCE.state.getBossbarIsland() != null;
    }

    @Override
    public StackElement init() {
        TextElement text = new TextElement(Component.literal("Island"));

        HStackElement hstack = new HStackElement()
                .add(new SpacerElement(4))
                .add(new VStackElement().add(new SpacerElement(1), text))
                .add(new SpacerElement(4));
        addNamedElement("text", text);

        return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
    }
}