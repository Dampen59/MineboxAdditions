package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class MuseumIndicator {
    public static void renderSlot(GuiGraphicsExtractor context, Slot slot, int sx, int sy) {
        if (!ItemsConfig.museumIndicator) return;
        if (MineboxAdditions.INSTANCE.state == null) return;
        if (!slot.hasItem()) return;

        List<String> missing = MineboxAdditions.INSTANCE.state.getMissingMuseumItemIds();
        if (missing == null || missing.isEmpty()) return;

        ItemStack stack = slot.getItem();
        if (!Utils.isMineboxItem(stack)) return;

        String id = Utils.getMineboxItemId(stack);
        if (id == null || id.isEmpty()) return;
        if (!missing.contains(id)) return;

        float hueOffset = ((System.currentTimeMillis() % 6000L) / 6000f);
        drawMuseumBorder(context, sx, sy, hueOffset);
    }

    private static void drawMuseumBorder(GuiGraphicsExtractor ctx, int x, int y, float hueOffset) {
        int top    = hsvToArgb((hueOffset + 0.00f) % 1f);
        int right  = hsvToArgb((hueOffset + 0.25f) % 1f);
        int bottom = hsvToArgb((hueOffset + 0.50f) % 1f);
        int left   = hsvToArgb((hueOffset + 0.75f) % 1f);

        ctx.fill(x,          y,          x + 16, y + 1,  top);
        ctx.fill(x + 15,     y,          x + 16, y + 16, right);
        ctx.fill(x,          y + 15,     x + 16, y + 16, bottom);
        ctx.fill(x,          y,          x + 1,  y + 16, left);
    }

    private static int hsvToArgb(float hue) {
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }
}
