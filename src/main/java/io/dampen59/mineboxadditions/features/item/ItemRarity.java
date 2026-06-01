package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.awt.*;

public class ItemRarity {
    public static void render(GuiGraphicsExtractor context, AbstractContainerScreen<?> screen) {
        if (!ItemsConfig.rarity.enabled) return;

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.isActive() || !slot.hasItem()) continue;

            ItemStack stack = slot.getItem();
            if (!Utils.isMineboxItem(stack)) continue;

            Color rarity = RaritiesUtils.getItemRarityColorFromLore(stack);
            if (rarity == null) continue;

            int argb = rarity.getRGB();

            if (ItemsConfig.rarity.mode == io.dampen59.mineboxadditions.config.items.objects.ItemRarity.Mode.CIRCLE) {
                drawCircle(context, slot.x, slot.y, argb);
            } else if (ItemsConfig.rarity.mode == io.dampen59.mineboxadditions.config.items.objects.ItemRarity.Mode.FILL) {
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, argb);
            }
        }
    }

    private static void drawCircle(GuiGraphicsExtractor ctx, int x, int y, int argb) {
        int cx = x + 8, cy = y + 8, r = 8;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, argb);
        }
    }
}