package io.dampen59.mineboxadditions.features.hud.huds.itempickup;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.huds.HaversackHud;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPickupManager {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final List<ItemPickupNotification> itemPickupNotifications = new ArrayList<>();

    public ItemPickupManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handle);
        HudRenderCallback.EVENT.register(this::render);
    }

    private void handle(MinecraftClient client) {
        if (MineboxAdditionConfig.get().displaySettings.itemPickupSettings.displayItemsPickups) {
            int displayDurationTicks = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.pickupNotificationDuration * 20;
            int maxNotifications = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.maxPickupNotifications;
            boolean mergeNotifications = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.mergeLines;
            updateInventorySnapshot(displayDurationTicks, maxNotifications, mergeNotifications);
            tickItemPickupNotifications();
        }
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        ItemPickupHud pickupHud = (ItemPickupHud) HudManager.INSTANCE.getHud(Hud.Type.ITEM_PICKUP);
        if (pickupHud.getState()) {
            for (int i = 0; i < itemPickupNotifications.size(); i++) {
                ItemPickupNotification notification = itemPickupNotifications.get(i);
                int offsetY = (i * (pickupHud.getHeight() + 2));
                pickupHud.setItem(notification.itemStack);
                pickupHud.setCount(notification.count);
                pickupHud.drawWithItem(context, offsetY);
            }
        }
    }

    private void updateInventorySnapshot(int notificationDuration, int maxNotifications, boolean mergeNotifications) {
        if (client.currentScreen != null || client.player == null || client.player.getInventory() == null) return;
        DefaultedList<ItemStack> currentInventory = client.player.getInventory().getMainStacks();
        for (int slot = 0; slot < currentInventory.size(); slot++) {
            ItemStack currentStack = currentInventory.get(slot);
            int currentCount = currentStack.getCount();
            int previousCount = previousInventoryCounts.getOrDefault(slot, 0);
            if (!currentStack.isEmpty() && currentCount > previousCount) {
                int gainedCount = currentCount - previousCount;
                addOrUpdateItemPickup(currentStack.copy(), gainedCount, notificationDuration, maxNotifications, mergeNotifications);
            }
        }
        updatePreviousInventoryCounts(currentInventory);
    }

    private void tickItemPickupNotifications() {
        itemPickupNotifications.removeIf(notification -> --notification.displayTicks <= 0);
    }

    private void updatePreviousInventoryCounts(DefaultedList<ItemStack> currentInventory) {
        previousInventoryCounts.clear();
        for (int slot = 0; slot < currentInventory.size(); slot++) {
            previousInventoryCounts.put(slot, currentInventory.get(slot).getCount());
        }
    }

    private void addOrUpdateItemPickup(ItemStack stack, int count, int duration, int maxNotifications, boolean merge) {
        if (Utils.isInventoryBaseItem(stack)) return;
        if (merge) {
            for (ItemPickupNotification notif : itemPickupNotifications) {
                if (ItemStack.areItemsAndComponentsEqual(notif.itemStack, stack)) {
                    notif.count += count;
                    notif.displayTicks = duration;
                    return;
                }
            }
        }
        itemPickupNotifications.add(new ItemPickupNotification(stack, count, duration));
        if (itemPickupNotifications.size() > maxNotifications) {
            itemPickupNotifications.removeFirst();
        }
    }

    private static class ItemPickupNotification {
        final ItemStack itemStack;
        int count;
        int displayTicks;

        ItemPickupNotification(ItemStack itemStack, int count, int displayTicks) {
            this.itemStack = itemStack;
            this.count = count;
            this.displayTicks = displayTicks;
        }
    }
}