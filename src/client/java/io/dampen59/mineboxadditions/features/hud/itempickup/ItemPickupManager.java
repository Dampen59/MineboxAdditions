package io.dampen59.mineboxadditions.features.hud.itempickup;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class ItemPickupManager {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final Deque<ItemPickupNotification> itemPickupNotifications = new ArrayDeque<>();

    public ItemPickupManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register(this::onRender);
    }

    private void onTick(MinecraftClient client) {
        var settings = MineboxAdditionConfig.get().displaySettings.itemPickupSettings;
        if (!settings.displayItemsPickups) return;

        int displayDuration = settings.pickupNotificationDuration * 20;
        updateInventorySnapshot(displayDuration, settings.maxPickupNotifications, settings.mergeLines);
        tickNotifications();
    }

    private void onRender(DrawContext context, RenderTickCounter tickCounter) {
        var hud = (ItemPickupHud) HudManager.INSTANCE.getHud(Hud.Type.ITEM_PICKUP);
        if (!hud.getState()) return;

        int offsetY = 0;
        for (ItemPickupNotification notif : itemPickupNotifications) {
            hud.setItem(notif.stack);
            hud.setCount(notif.count);
            hud.drawWithItem(context, offsetY);
            offsetY += hud.getHeight() + 2;
        }
    }

    private void updateInventorySnapshot(int duration, int max, boolean merge) {
        if (client.currentScreen != null || client.player == null) return;

        DefaultedList<ItemStack> currentInv = client.player.getInventory().getMainStacks();
        for (int slot = 0; slot < currentInv.size(); slot++) {
            ItemStack currentStack = currentInv.get(slot);
            int currentCount = currentStack.getCount();
            int previousCount = previousInventoryCounts.getOrDefault(slot, 0);

            if (!currentStack.isEmpty() && currentCount > previousCount) {
                int gained = currentCount - previousCount;
                addOrUpdateNotification(currentStack.copy(), gained, duration, max, merge);
            }
        }

        previousInventoryCounts.clear();
        for (int i = 0; i < currentInv.size(); i++) {
            previousInventoryCounts.put(i, currentInv.get(i).getCount());
        }
    }

    private void tickNotifications() {
        itemPickupNotifications.removeIf(notif -> --notif.displayTicks <= 0);
    }

    private void addOrUpdateNotification(ItemStack stack, int count, int duration, int max, boolean merge) {
        if (Utils.isInventoryBaseItem(stack)) return;

        if (merge) {
            for (ItemPickupNotification notif : itemPickupNotifications) {
                if (ItemStack.areItemsAndComponentsEqual(notif.stack, stack)) {
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