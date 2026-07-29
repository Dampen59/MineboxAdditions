package io.dampen59.mineboxadditions.features.hud.huds.itempickup;

import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.*;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemPickupHud extends Hud {
    public ItemPickupHud() {
        super(
                () -> HudsConfig.itempickup.enabled,
                s -> HudsConfig.itempickup.enabled = s,
                () -> HudPositions.itempickup.x,
                x -> HudPositions.itempickup.x = x,
                () -> HudPositions.itempickup.y,
                y -> HudPositions.itempickup.y = y);
    }

    @Override
    public StackElement init() {
        ItemStack stack = ItemStack.EMPTY;

        ItemStackElement item = new ItemStackElement(stack);
        VStackElement vstack1 = new VStackElement()
                .add(new SpacerElement(1))
                .add(new HStackElement().add(new SpacerElement(1), item, new SpacerElement(1)))
                .add(new SpacerElement(1));
        addNamedElement("item", item);
        addNamedElement("vstack1", vstack1);

        TextElement name = new TextElement(net.minecraft.network.chat.Component.empty(), 100);
        VStackElement vstack2 = new VStackElement()
                .add(new SpacerElement(5))
                .add(new HStackElement().add(new SpacerElement(4), name, new SpacerElement(4)))
                .add(new SpacerElement(4));
        addNamedElement("name", name);
        addNamedElement("vstack2", vstack2);

        return new HStackElement().add(vstack1, new SpacerElement(2), vstack2);
    }

    @Override
    public void draw(GuiGraphicsExtractor context) {
        this.draw(context, 0);
    }

    public void draw(GuiGraphicsExtractor context, int offset) {
        if (getX() == -50) {
            Minecraft client = Minecraft.getInstance();
            int screenWidth = client.getWindow().getGuiScaledWidth();
            setX(screenWidth - this.getWidth() - 4);
            ConfigManager.save();
        }

        getNamedElement("vstack1", VStackElement.class).setColor(0x40000000);
        getNamedElement("vstack2", VStackElement.class).setColor(0x40000000);
        mainStack.draw(context, getX(), getY() + offset);
    }

    @Override
    public void drawDisabled(GuiGraphicsExtractor context) {
        getNamedElement("vstack1", VStackElement.class).setColor(0x40FF0000);
        getNamedElement("vstack2", VStackElement.class).setColor(0x40FF0000);
        mainStack.draw(context, getX(), getY());
    }
}
