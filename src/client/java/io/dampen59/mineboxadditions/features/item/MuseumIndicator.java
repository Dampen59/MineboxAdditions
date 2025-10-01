package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class MuseumIndicator {
    public static void render(DrawContext context, HandledScreen<?> screen) {
        if (!ItemsConfig.museumIndicator) return;
        if (MineboxAdditions.INSTANCE.state == null) return;
        List<String> missing = MineboxAdditions.INSTANCE.state.getMissingMuseumItemIds();
        if (missing == null || missing.isEmpty()) return;

        final float hueOffset = ((System.currentTimeMillis() % 6000L) / 6000f);

        for (Slot slot : screen.getScreenHandler().slots) {
            if (!slot.isEnabled() || !slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            if (!Utils.isMineboxItem(stack)) continue;
            String id = Utils.getMineboxItemId(stack);
            if (id == null || id.isEmpty()) continue;
            if (!missing.contains(id)) continue;
            drawMuseumBorder(context, slot.x, slot.y, hueOffset);
        }
    }

    private static void drawMuseumBorder(DrawContext ctx, int x, int y, float hueOffset) {
        int top    = hsvToArgb((hueOffset + 0.00f) % 1f);
        int right  = hsvToArgb((hueOffset + 0.25f) % 1f);
        int bottom = hsvToArgb((hueOffset + 0.50f) % 1f);
        int left   = hsvToArgb((hueOffset + 0.75f) % 1f);

        ctx.fill(x, y, x + 16, y + 1, top);
        ctx.fill(x + 16 - 1, y, x + 16, y + 16, right);
        ctx.fill(x, y + 16 - 1, x + 16, y + 16, bottom);
        ctx.fill(x, y, x + 1, y + 16, left);
    }

    private static int hsvToArgb(float hue) {
        int rgb = java.awt.Color.HSBtoRGB(hue, (float) 1.0, (float) 1.0);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }
}