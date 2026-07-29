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

public class BossBarHuds {
    public static class KeyFragmentHud extends Hud {
        public KeyFragmentHud() {
            super(
                    () -> MineboxDefaultHuds.keyfragment,
                    s -> MineboxDefaultHuds.keyfragment = s,
                    () -> HudPositions.keyfragment.x,
                    x -> HudPositions.keyfragment.x = x,
                    () -> HudPositions.keyfragment.y,
                    y -> HudPositions.keyfragment.y = y);

            ClientTickEvents.END_CLIENT_TICK.register(this::update);
        }

        @Override
        public StackElement init() {
            TextElement text = new TextElement(Component.literal("Key fragment giveall in 00:00"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new VStackElement().add(new SpacerElement(1), text))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        private void update(Minecraft client) {
            Component value = MineboxAdditions.INSTANCE.state.getBossbarKeyFragment();
            if (value != null) {
                getNamedElement("text", TextElement.class).setValue(value);
            }
        }

        @Override
        public boolean shouldRender() {
            return MineboxAdditions.INSTANCE.state.getBossbarKeyFragment() != null;
        }
    }

    public static class StatsHud extends Hud {
        public StatsHud() {
            super(
                    () -> MineboxDefaultHuds.statspoints,
                    s -> MineboxDefaultHuds.statspoints = s,
                    () -> HudPositions.statspoints.x,
                    x -> HudPositions.statspoints.x = x,
                    () -> HudPositions.statspoints.y,
                    y -> HudPositions.statspoints.y = y);

            ClientTickEvents.END_CLIENT_TICK.register(this::update);
        }

        @Override
        public StackElement init() {
            TextElement text = new TextElement(Component.literal("+0 stats points available! /stats"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new VStackElement().add(new SpacerElement(1), text))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        private void update(Minecraft client) {
            Component value = MineboxAdditions.INSTANCE.state.getBossbarStatsPoints();
            if (value != null) {
                getNamedElement("text", TextElement.class).setValue(value);
            }
        }

        @Override
        public boolean shouldRender() {
            return MineboxAdditions.INSTANCE.state.getBossbarStatsPoints() != null;
        }
    }

    public static class FreeItemHud extends Hud {
        public FreeItemHud() {
            super(
                    () -> MineboxDefaultHuds.freeitem,
                    s -> MineboxDefaultHuds.freeitem = s,
                    () -> HudPositions.freeitem.x,
                    x -> HudPositions.freeitem.x = x,
                    () -> HudPositions.freeitem.y,
                    y -> HudPositions.freeitem.y = y);

            ClientTickEvents.END_CLIENT_TICK.register(this::update);
        }

        @Override
        public StackElement init() {
            TextElement text = new TextElement(Component.literal("Free item available in /store"));

            HStackElement hstack = new HStackElement()
                    .add(new SpacerElement(4))
                    .add(new VStackElement().add(new SpacerElement(1), text))
                    .add(new SpacerElement(4));
            addNamedElement("text", text);

            return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
        }

        private void update(Minecraft client) {
            Component value = MineboxAdditions.INSTANCE.state.getBossbarFreeItem();
            if (value != null) {
                getNamedElement("text", TextElement.class).setValue(value);
            }
        }

        @Override
        public boolean shouldRender() {
            return MineboxAdditions.INSTANCE.state.getBossbarFreeItem() != null;
        }
    }
}
