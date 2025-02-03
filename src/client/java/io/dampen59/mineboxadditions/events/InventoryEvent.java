package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class InventoryEvent {
    private State modState = null;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Map<Integer, Integer> previousInventoryCounts = new HashMap<>();
    private final List<ItemPickupNotification> itemPickupNotifications = new ArrayList<>();

    public InventoryEvent(State prmModState) {
        this.modState = prmModState;
        HudRenderCallback.EVENT.register(this::renderItemsPickups);
        onTick();
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            if (client.player == null) return;
            if (client.world == null) return;
            if (!modState.getConnectedToMinebox()) return;

            client.player.getInventory().offHand.stream().filter(stack -> !stack.isEmpty()).forEach(this::handleDurability);
            client.player.getInventory().main.stream().filter(stack -> !stack.isEmpty()).forEach(this::handleDurability);

            if (config.displaySettings.itemPickupSettings.displayItemsPickups) {
                int displayDurationTicks = config.displaySettings.itemPickupSettings.pickupNotificationDuration * 20;
                int maxConccurentNotifications = config.displaySettings.itemPickupSettings.maxPickupNotifications;
                boolean shouldMergeNotifications = config.displaySettings.itemPickupSettings.mergeLines;
                updateInventorySnapshot(displayDurationTicks, maxConccurentNotifications, shouldMergeNotifications);
                tickItemsPickupNotifications();
            }
        });

    }

    private void tickItemsPickupNotifications() {
        itemPickupNotifications.removeIf(notification -> --notification.displayTicks <= 0);
    }

    private void updateInventorySnapshot(int notificationDisplayDuration, int maxNotifications, boolean shouldMerge) {
        if (client.currentScreen != null || client.player == null || client.player.getInventory() == null) return;

        DefaultedList<ItemStack> currentInventory = client.player.getInventory().main;

        for (int slot = 0; slot < currentInventory.size(); slot++) {
            ItemStack currentStack = currentInventory.get(slot);
            int currentCount = currentStack.getCount();
            int previousCount = previousInventoryCounts.getOrDefault(slot, 0);

            if (!currentStack.isEmpty() && currentCount > previousCount) {
                int gainedCount = currentCount - previousCount;
                addOrUpdateItemPickup(currentStack.copy(), gainedCount, notificationDisplayDuration, maxNotifications, shouldMerge);
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

    private void addOrUpdateItemPickup(ItemStack itemStack, int count, int displayDuration, int maxNotifications, boolean shouldMerge) {
        if (shouldMerge) {
            for (ItemPickupNotification notification : itemPickupNotifications) {
                if (ItemStack.areItemsAndComponentsEqual(notification.itemStack, itemStack)) {
                    notification.count += count;
                    notification.displayTicks = displayDuration;
                    return;
                }
            }
        }

        itemPickupNotifications.add(new ItemPickupNotification(itemStack, count, displayDuration));

        if (itemPickupNotifications.size() > maxNotifications) {
            itemPickupNotifications.remove(0);
        }
    }

    private void renderItemsPickups(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.displaySettings.itemPickupSettings.displayItemsPickups) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth / 2 + 10;
        int baseY = screenHeight / 2 - 20;

        for (int i = 0; i < itemPickupNotifications.size(); i++) {
            ItemPickupNotification notification = itemPickupNotifications.get(i);
            int y = baseY - (i * 20);

            drawContext.drawItem(notification.itemStack, x, y);
            String text = "+ " + notification.count + " ";
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(text).append(notification.itemStack.getName()), x + 20, y + 4, 0xFFFFFF);
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

    public void handleDurability(ItemStack prmItemStack) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        NbtComponent itemData = prmItemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;

        String id = nbtData.getString("mbitems:id");
        LoreComponent loresList = prmItemStack.get(DataComponentTypes.LORE);

        for (Text lore : loresList.lines()) {
            if (!(lore.getContent() instanceof TranslatableTextContent)) continue;

            TranslatableTextContent translatableContent = (TranslatableTextContent) lore.getContent();
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                if (config.durabilitySettings.haversackDurability) handleHaversackDurability(prmItemStack, nbtData, translatableContent);
            } else if (id.contains("harvester_") && translatableContent.getKey().contains("mbx.durability")) {
                if (config.durabilitySettings.harvesterDurability) handleHarvesterDurability(prmItemStack, translatableContent);
            }
        }
    }

    private void handleHaversackDurability(ItemStack prmItemStack, NbtCompound nbtData, TranslatableTextContent translatableContent) {
        StringVisitable haversackQuantity = translatableContent.getArg(0);
        String[] quantityParts = haversackQuantity.getString().split("/");
        int haversackMaxQuantity = Integer.parseInt(quantityParts[1]);

        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent");
        int amountInside = persistentData.getInt("mbitems:amount_inside");

        ComponentChanges haversackChanges = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, haversackMaxQuantity - amountInside)
                .add(DataComponentTypes.MAX_DAMAGE, haversackMaxQuantity)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();

        prmItemStack.setDamage(amountInside);
        prmItemStack.applyChanges(haversackChanges);
    }

    private void handleHarvesterDurability(ItemStack prmItemStack, TranslatableTextContent translatableContent) {
        StringVisitable durabilityInfo = translatableContent.getArg(0);
        String durabilityStr = durabilityInfo.getString();

        if (!durabilityStr.contains("/")) return; // Item not repairable

        String[] quantityParts = durabilityStr.split("/");
        int harvesterCurrentDurability = Integer.parseInt(quantityParts[0]);
        int harvesterMaxDurability = Integer.parseInt(quantityParts[1]);

        ComponentChanges harvesterChanges = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, harvesterMaxDurability - harvesterCurrentDurability)
                .add(DataComponentTypes.MAX_DAMAGE, harvesterMaxDurability)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();

        prmItemStack.setDamage(harvesterMaxDurability - harvesterCurrentDurability);
        prmItemStack.applyChanges(harvesterChanges);
    }


}
