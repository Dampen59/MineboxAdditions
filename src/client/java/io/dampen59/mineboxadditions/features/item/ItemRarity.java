package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.awt.*;

public class ItemRarity {
    public static void render(DrawContext context, HandledScreen<?> screen) {
        if (!MineboxAdditionConfig.get().displaySettings.itemRaritySettings.displayItemsRarity) return;

        for (Slot slot : screen.getScreenHandler().slots) {
            if (!slot.isEnabled() || !slot.hasStack()) continue;

            ItemStack stack = slot.getStack();
            if (!Utils.isMineboxItem(stack)) continue;

            Color rarity = RaritiesUtils.getItemRarityColorFromLore(stack);
            if (rarity == null) continue;

            int argb = rarity.getRGB();

            if (MineboxAdditionConfig.get().displaySettings.itemRaritySettings.displayMode == MineboxAdditionConfig.RaritiesDisplayMode.CIRCLE) {
                drawCircle(context, slot.x, slot.y, argb);
            } else if (MineboxAdditionConfig.get().displaySettings.itemRaritySettings.displayMode == MineboxAdditionConfig.RaritiesDisplayMode.FILL) {
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, argb);
            }
        }
    }

    private static void drawCircle(DrawContext ctx, int x, int y, int argb) {
        int cx = x + 8, cy = y + 8, r = 8;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, argb);
        }
    }
}