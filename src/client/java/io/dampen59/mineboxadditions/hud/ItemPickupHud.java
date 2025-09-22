package io.dampen59.mineboxadditions.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemPickupHud extends Hud {
    private ItemStack item;

    public ItemPickupHud(Supplier<Integer> getX, Supplier<Integer> getY,
                         Consumer<Integer> setX, Consumer<Integer> setY,
                         ItemStack item) {
        super(getX, getY, setX, setY, null, null);
        this.item = item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public int getWidth() {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        return 28 + renderer.getWidth(item.getName());
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void draw(DrawContext context) {
        this.drawWithItem(context, 0);
    }

    public void drawWithItem(DrawContext context, int offsetY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int x = getX.get();
        if (x > screenWidth / 2) x -= this.getWidth();
        int y = getY.get() + offsetY;

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        this.drawPlate(context, x, y, 18, this.getHeight());
        this.drawPlate(context, x + 20, y, renderer.getWidth(item.getName()) + 8, this.getHeight());
        context.drawItem(item, x + 1, y + 1);

        String count = String.valueOf(item.getCount());
        context.drawText(renderer, count, x + (this.getHeight() - renderer.getWidth(count)), y + 1 + (this.getHeight() - renderer.fontHeight), 0xFFFFFFFF, true);

        int textY = (this.getHeight() - renderer.fontHeight) / 2 + (this.getHeight() - renderer.fontHeight) % 2;
        context.drawText(renderer, item.getName(), x + 24, y + textY, 0xFFFFFFFF, true);
    }
}
