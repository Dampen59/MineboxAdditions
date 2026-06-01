package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class MuseumIndicator {
    public static void render(GuiGraphicsExtractor context, AbstractContainerScreen<?> screen) {
        if (!ItemsConfig.museumIndicator) return;
        if (MineboxAdditions.INSTANCE.state == null) return;
        List<String> missing = MineboxAdditions.INSTANCE.state.getMissingMuseumItemIds();
        if (missing == null || missing.isEmpty()) return;

        final float hueOffset = ((System.currentTimeMillis() % 6000L) / 6000f);

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.isActive() || !slot.hasItem()) continue;
            ItemStack stack = slot.getItem();
            if (!Utils.isMineboxItem(stack)) continue;
            String id = Utils.getMineboxItemId(stack);
            if (id == null || id.isEmpty()) continue;
            if (!missing.contains(id)) continue;
            drawMuseumBorder(context, slot.x, slot.y, hueOffset);
        }
    }

    private static void drawMuseumBorder(GuiGraphicsExtractor ctx, int x, int y, float hueOffset) {
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