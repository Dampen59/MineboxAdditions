package io.dampen59.mineboxadditions.events.inventory;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.huds.ItemPickupHud;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class InventoryEvent {
    private final State modState;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final List<ItemPickupNotification> itemPickupNotifications = new ArrayList<>();

    private int lastAmountInside = -1;
    private long lastCheckTime = System.currentTimeMillis();
    private double fillRatePerSecond = 0.0;
    private String timeUntilFull = "";
    public InventoryEvent(State modState) {
        this.modState = modState;
        HudRenderCallback.EVENT.register(this::renderItemsPickups);
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private void onTick() {
        if (client.player == null || client.world == null || !modState.isConnectedToMinebox()) return;

        Stream.of(client.player.getOffHandStack())
                .filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);
        client.player.getInventory().getMainStacks().stream().filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);

        if (client.currentScreen instanceof GenericContainerScreen containerScreen) {
            containerScreen.getScreenHandler().slots.forEach(slot -> {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    handleDurability(stack);
                }
            });
        }

        if (MineboxAdditionConfig.get().displaySettings.itemPickupSettings.displayItemsPickups) {
            int displayDurationTicks = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.pickupNotificationDuration * 20;
            int maxNotifications = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.maxPickupNotifications;
            boolean mergeNotifications = MineboxAdditionConfig.get().displaySettings.itemPickupSettings.mergeLines;
            updateInventorySnapshot(displayDurationTicks, maxNotifications, mergeNotifications);
            tickItemPickupNotifications();
        }
    }

    private void tickItemPickupNotifications() {
        itemPickupNotifications.removeIf(notification -> --notification.displayTicks <= 0);
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

    private void renderItemsPickups(DrawContext context, RenderTickCounter tickCounter) {
        if (!MineboxAdditionConfig.get().displaySettings.itemPickupSettings.displayItemsPickups) return;

        if (MineboxAdditionConfig.get().itemPickupHudX == 0 || MineboxAdditionConfig.get().itemPickupHudY == 0) {
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            MineboxAdditionConfig.get().itemPickupHudX = screenWidth / 2 + 10;
            MineboxAdditionConfig.get().itemPickupHudY = screenHeight / 2 - 20;
            MineboxAdditionConfig.save();
        }

        ItemPickupHud pickupHud = (ItemPickupHud) HudManager.INSTANCE.getHud(Hud.Type.ITEM_PICKUP);

        for (int i = 0; i < itemPickupNotifications.size(); i++) {
            ItemPickupNotification notification = itemPickupNotifications.get(i);
            int offsetY = (i * (pickupHud.getHeight() + 2));
            pickupHud.setItem(notification.itemStack);
            pickupHud.setCount(notification.count);
            pickupHud.drawWithItem(context, offsetY);
        }

        if (fillRatePerSecond != 0) {
            if (MineboxAdditionConfig.get().displaySettings.displayHaversackFillRate) {
                String rateText = String.format("Fill Rate: %.2f/s", fillRatePerSecond);
                Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_RATE);
                hud.setText(Text.of(rateText));
                hud.draw(context);
            }

            if (MineboxAdditionConfig.get().displaySettings.displayHaversackFullIn) {
                String timeText = "Full in: " + timeUntilFull;
                Hud hud = HudManager.INSTANCE.getHud(Hud.Type.HAVERSACK_FULL);
                hud.setText(Text.of(timeText));
                hud.draw(context);
            }
        }

    }

    private void handleDurability(ItemStack stack) {
        var itemData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return;
        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;
        String id = nbtData.getString("mbitems:id").orElse("");
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) return;

        for (Text lore : loreComponent.lines()) {
            if (!(lore.getContent() instanceof TranslatableTextContent translatableContent)) continue;
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                if (MineboxAdditionConfig.get().durabilitySettings.haversackDurability) {
                    handleHaversackDurability(stack, nbtData, translatableContent);
                }
            }
        }
    }

    private void handleHaversackDurability(ItemStack stack, NbtCompound nbtData, TranslatableTextContent content) {
        StringVisitable quantityArg = content.getArg(0);
        String[] parts = quantityArg.getString().split("/");
        if (parts.length < 2) return;
        int maxQuantity = Integer.parseInt(parts[1]);
        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
        int amountInside = persistentData.getInt("mbitems:amount_inside").orElse(0);

        long currentTime = System.currentTimeMillis();
        if (lastAmountInside >= 0) {
            long deltaTime = currentTime - lastCheckTime;
            if (deltaTime >= 1000) {
                int deltaAmount = amountInside - lastAmountInside;
                fillRatePerSecond = deltaAmount / (deltaTime / 1000.0);
                lastCheckTime = currentTime;
                lastAmountInside = amountInside;

                // Estimate time to full
                int remaining = maxQuantity - amountInside;
                if (fillRatePerSecond > 0) {
                    long secondsLeft = (long) (remaining / fillRatePerSecond);
                    timeUntilFull = Utils.formatTime(secondsLeft);
                } else {
                    timeUntilFull = "∞";
                }
            }
        } else {
            lastAmountInside = amountInside;
            lastCheckTime = currentTime;
            timeUntilFull = "";
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
