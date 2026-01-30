package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

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
    public void draw(DrawContext context, int x, int y) {
        context.drawItem(item, x, y);

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        String count = String.valueOf(item.getCount());
        context.drawText(renderer, count, x + 1 + (this.getHeight() - renderer.getWidth(count)), y + 2 + (this.getHeight() - renderer.fontHeight), 0xFFFFFFFF, true);
    }
}
