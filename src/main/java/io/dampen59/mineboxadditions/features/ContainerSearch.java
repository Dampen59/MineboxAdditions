package io.dampen59.mineboxadditions.features;

import io.dampen59.mineboxadditions.config.render.RenderConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerSearch {
    private static String query = "";

    public static String getQuery() {
        return query;
    }

    public static void setQuery(String q) {
        query = (q == null) ? "" : q;
    }

    public static boolean isActive() {
        return RenderConfig.containerSearch && !query.isBlank();
    }

    public static boolean matchesSlot(Slot slot) {
        if (!isActive()) return true;
        if (!slot.hasItem()) return false;

        ItemStack stack = slot.getItem();
        String q = query.toLowerCase();

        if (stack.getHoverName().getString().toLowerCase().contains(q)) return true;

        String id = Utils.getMineboxItemId(stack);
        return id != null && id.toLowerCase().contains(q);
    }

    public static void renderSlotOverlay(GuiGraphicsExtractor context, Slot slot) {
        if (!isActive() || !slot.hasItem()) return;
        if (matchesSlot(slot)) return;
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0xAA000000);
    }
}
