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

public class TimeHud extends Hud {
    public TimeHud() {
        super(
                () -> MineboxDefaultHuds.time,
                s -> MineboxDefaultHuds.time = s,
                () -> HudPositions.time.x,
                x -> HudPositions.time.x = x,
                () -> HudPositions.time.y,
                y -> HudPositions.time.y = y);

        ClientTickEvents.END_CLIENT_TICK.register(this::update);
    }

    private void update(Minecraft client) {
        Component time = MineboxAdditions.INSTANCE.state.getBossbarTime();
        if (time != null) {
            getNamedElement("text", TextElement.class).setValue(time);
        }
    }

    @Override
    public boolean shouldRender() {
        return MineboxAdditions.INSTANCE.state.getBossbarTime() != null;
    }

    @Override
    public StackElement init() {
        TextElement text = new TextElement(Component.literal("00:00"));

        HStackElement hstack = new HStackElement()
                .add(new SpacerElement(4))
                .add(new VStackElement().add(new SpacerElement(1), text))
                .add(new SpacerElement(4));
        addNamedElement("text", text);

        return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
    }
}