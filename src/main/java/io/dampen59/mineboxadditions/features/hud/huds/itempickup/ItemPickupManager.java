package io.dampen59.mineboxadditions.features.hud.huds.itempickup;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.ItemStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ItemPickupManager {
    private final Minecraft client = Minecraft.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final Deque<ItemPickupNotification> itemPickupNotifications = new ArrayDeque<>();

    public ItemPickupManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath("mineboxadditions", "itempickup"), this::onRender);
    }

    private void onTick(Minecraft client) {
        var settings = HudsConfig.itempickup;
        if (!settings.enabled) return;

        int displayDuration = settings.duration * 20;
        updateInventorySnapshot(displayDuration, settings.count, settings.merge);
        tickNotifications();
    }

    private void onRender(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        var hud = HudManager.INSTANCE.get(ItemPickupHud.class);
        int offsetY = 0;
        for (ItemPickupNotification notif : itemPickupNotifications) {
            ItemStack item = notif.stack.copy();
            item.setCount(notif.count);
            hud.getNamedElement("item", ItemStackElement.class).setItem(item);
            hud.getNamedElement("name", TextElement.class).setValue(item.getHoverName());
            if (hud.getState()) hud.draw(context, offsetY);
            offsetY += hud.getHeight() + 2;
        }
    }

    private void updateInventorySnapshot(int duration, int max, boolean merge) {
        if (client.gui.screen() != null || client.player == null) return;

        var inv = client.player.getInventory();
        int invSize = inv.getContainerSize();
        for (int slot = 0; slot < invSize; slot++) {
            ItemStack currentStack = inv.getItem(slot);
            int currentCount = currentStack.getCount();
            int previousCount = previousInventoryCounts.getOrDefault(slot, 0);

            if (!currentStack.isEmpty() && currentCount > previousCount) {
                int gained = currentCount - previousCount;
                addOrUpdateNotification(currentStack.copy(), gained, duration, max, merge);
            }
        }

        previousInventoryCounts.clear();
        for (int i = 0; i < invSize; i++) {
            previousInventoryCounts.put(i, inv.getItem(i).getCount());
        }
    }

    private void tickNotifications() {
        itemPickupNotifications.removeIf(notif -> --notif.displayTicks <= 0);
    }

    private void addOrUpdateNotification(ItemStack stack, int count, int duration, int max, boolean merge) {
        if (Utils.isInventoryBaseItem(stack)) return;

        if (merge) {
            for (ItemPickupNotification notif : itemPickupNotifications) {
                if (ItemStack.isSameItemSameComponents(stripLore(notif.stack), stripLore(stack))) {
                    notif.add(count, duration);
                    return;
                }
            }
        }

        itemPickupNotifications.add(new ItemPickupNotification(stack, count, duration));
        if (itemPickupNotifications.size() > max) {
            itemPickupNotifications.removeFirst();
        }
    }

    private static ItemStack stripLore(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.remove(DataComponents.LORE);
        return copy;
    }

    private static class ItemPickupNotification {
        final ItemStack stack;
        int count;
        int displayTicks;

        ItemPickupNotification(ItemStack stack, int count, int displayTicks) {
            this.stack = stack;
            this.count = count;
            this.displayTicks = displayTicks;
        }

        void add(int more, int resetDuration) {
            this.count += more;
            this.displayTicks = resetDuration;
        }
    }
}