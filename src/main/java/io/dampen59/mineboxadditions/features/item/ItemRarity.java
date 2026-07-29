package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.awt.*;

public class ItemRarity {
    public static void renderSlot(GuiGraphicsExtractor context, Slot slot, int sx, int sy) {
        if (!ItemsConfig.rarity.enabled) return;
        if (!slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        if (!Utils.isMineboxItem(stack)) return;

        Color rarity = RaritiesUtils.getItemRarityColorFromLore(stack);
        if (rarity == null) return;

        int argb = rarity.getRGB();

        if (ItemsConfig.rarity.mode == io.dampen59.mineboxadditions.config.items.objects.ItemRarity.Mode.CIRCLE) {
            drawCircle(context, sx, sy, argb);
        } else if (ItemsConfig.rarity.mode == io.dampen59.mineboxadditions.config.items.objects.ItemRarity.Mode.FILL) {
            context.fill(sx, sy, sx + 16, sy + 16, argb);
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
