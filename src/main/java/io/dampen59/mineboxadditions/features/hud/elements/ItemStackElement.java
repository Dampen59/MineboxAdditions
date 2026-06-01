package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemStackElement extends Element {
    private ItemStack item;

    public ItemStackElement(ItemStack item) {
        setItem(item);
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(GuiGraphicsExtractor context, int x, int y) {
        context.item(item, x, y);

        Font renderer = Minecraft.getInstance().font;
        String count = String.valueOf(item.getCount());
        context.text(renderer, count, x + 1 + (this.getHeight() - renderer.width(count)), y + 2 + (this.getHeight() - renderer.lineHeight), 0xFFFFFFFF, true);
    }
}
