package io.dampen59.mineboxadditions.features.hud.huds.itempickup;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemPickupHud extends Hud {
    private ItemStack item;
    private int count;

    public ItemPickupHud() {
        super(
                () -> MineboxAdditionConfig.get().displaySettings.itemPickupSettings.displayItemsPickups,
                s -> MineboxAdditionConfig.get().displaySettings.itemPickupSettings.displayItemsPickups = s,
                () -> MineboxAdditionConfig.get().itemPickupHudX,
                () -> MineboxAdditionConfig.get().itemPickupHudY,
                x -> MineboxAdditionConfig.get().itemPickupHudX = x,
                y -> MineboxAdditionConfig.get().itemPickupHudY = y,
                null, null);
        this.item = new ItemStack(Items.DIAMOND);
        this.count = item.getCount();
    }

    public void setCount(int count) {
        this.count = count;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public int getX() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int x = super.getX();
        if (x > screenWidth / 2) {
            return x - this.getWidth();
        }
        return x;
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
    public void draw(DrawContext context, int color) {
        if (getX() == -50) {
            MinecraftClient client = MinecraftClient.getInstance();
            int screenWidth = client.getWindow().getScaledWidth();
            setX(screenWidth - 4);
            MineboxAdditionConfig.save();
        }
        this.drawWithItem(context, 0, color);
    }

    public void drawWithItem(DrawContext context, int offsetY) {
        drawWithItem(context, offsetY, 0x40000000);
    }

    public void drawWithItem(DrawContext context, int offsetY, int color) {
        int x = getX();
        int y = getY() + offsetY;

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        this.drawPlate(context, x, y, 18, this.getHeight(), color);
        this.drawPlate(context, x + 20, y, renderer.getWidth(item.getName()) + 8, this.getHeight(), color);
        context.drawItem(item, x + 1, y + 1);

        String count = String.valueOf(this.count);
        context.drawText(renderer, count, x + (this.getHeight() - renderer.getWidth(count)), y + 1 + (this.getHeight() - renderer.fontHeight), 0xFFFFFFFF, true);

        int textY = (this.getHeight() - renderer.fontHeight) / 2 + (this.getHeight() - renderer.fontHeight) % 2;
        context.drawText(renderer, item.getName(), x + 24, y + textY, 0xFFFFFFFF, true);
    }
}
