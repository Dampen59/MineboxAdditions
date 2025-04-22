package io.dampen59.mineboxadditions.events.inventory;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.ComponentChanges;
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

public class InventoryEvent {
    private final State modState;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final List<ItemPickupNotification> itemPickupNotifications = new ArrayList<>();

    public InventoryEvent(State modState) {
        this.modState = modState;
        HudRenderCallback.EVENT.register(this::renderItemsPickups);
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick());
    }

    private void onTick() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (client.player == null || client.world == null || !modState.getConnectedToMinebox()) return;

        client.player.getInventory().offHand.stream().filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);
        client.player.getInventory().main.stream().filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);

        if (client.currentScreen instanceof GenericContainerScreen containerScreen) {
            containerScreen.getScreenHandler().slots.forEach(slot -> {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    handleDurability(stack);
                }
            });
        }

        if (config.displaySettings.itemPickupSettings.displayItemsPickups) {
            int displayDurationTicks = config.displaySettings.itemPickupSettings.pickupNotificationDuration * 20;
            int maxNotifications = config.displaySettings.itemPickupSettings.maxPickupNotifications;
            boolean mergeNotifications = config.displaySettings.itemPickupSettings.mergeLines;
            updateInventorySnapshot(displayDurationTicks, maxNotifications, mergeNotifications);
            tickItemPickupNotifications();
        }
    }

    private void tickItemPickupNotifications() {
        itemPickupNotifications.removeIf(notification -> --notification.displayTicks <= 0);
    }

    private void updateInventorySnapshot(int notificationDuration, int maxNotifications, boolean mergeNotifications) {
        if (client.currentScreen != null || client.player == null || client.player.getInventory() == null) return;
        DefaultedList<ItemStack> currentInventory = client.player.getInventory().main;
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

    private void renderItemsPickups(DrawContext drawContext, RenderTickCounter tickCounter) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.displaySettings.itemPickupSettings.displayItemsPickups) return;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2 + 10;
        int baseY = screenHeight / 2 - 20;
        for (int i = 0; i < itemPickupNotifications.size(); i++) {
            ItemPickupNotification notif = itemPickupNotifications.get(i);
            int y = baseY - (i * 20);
            drawContext.drawItem(notif.itemStack, x, y);
            String text = "+ " + notif.count + " ";
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(text).append(notif.itemStack.getName()), x + 20, y + 4, 0xFFFFFF);
        }
    }

    private void handleDurability(ItemStack stack) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        var itemData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return;
        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;
        String id = nbtData.getString("mbitems:id");
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) return;

        for (Text lore : loreComponent.lines()) {
            if (!(lore.getContent() instanceof TranslatableTextContent translatableContent)) continue;
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                if (config.durabilitySettings.haversackDurability) {
                    handleHaversackDurability(stack, nbtData, translatableContent);
                }
            } else if (id.contains("harvester_") && translatableContent.getKey().contains("mbx.durability")) {
                if (config.durabilitySettings.harvesterDurability) {
                    handleHarvesterDurability(stack, translatableContent);
                }
            }
        }
    }

    private void handleHaversackDurability(ItemStack stack, NbtCompound nbtData, TranslatableTextContent content) {
        StringVisitable quantityArg = content.getArg(0);
        String[] parts = quantityArg.getString().split("/");
        if (parts.length < 2) return;
        int maxQuantity = Integer.parseInt(parts[1]);
        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent");
        int amountInside = persistentData.getInt("mbitems:amount_inside");
        ComponentChanges changes = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, maxQuantity - amountInside)
                .add(DataComponentTypes.MAX_DAMAGE, maxQuantity)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();
        stack.setDamage(amountInside);
        stack.applyChanges(changes);
    }

    private void handleHarvesterDurability(ItemStack stack, TranslatableTextContent content) {
        StringVisitable durabilityArg = content.getArg(0);
        String durabilityStr = durabilityArg.getString();
        if (!durabilityStr.contains("/")) return;
        String[] parts = durabilityStr.split("/");
        if (parts.length < 2) return;
        int currentDurability = Integer.parseInt(parts[0]);
        int maxDurability = Integer.parseInt(parts[1]);
        ComponentChanges changes = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, maxDurability - currentDurability)
                .add(DataComponentTypes.MAX_DAMAGE, maxDurability)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();
        stack.setDamage(maxDurability - currentDurability);
        stack.applyChanges(changes);
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
