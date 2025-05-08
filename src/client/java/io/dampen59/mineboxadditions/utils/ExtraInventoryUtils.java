package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.minebox.ExtraInventoryItem;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtraInventoryUtils {

    private static final Map<Integer, String> SLOT_CATEGORY_MAP;

    static {

        SLOT_CATEGORY_MAP = Map.of(2, "Helmet",
                11, "Chestplate",
                20, "Leggings",
                29, "Boots",
                1, "Necklace",
                3, "Ring1",
                12, "Ring2",
                19, "Belt",
                10, "Backpack"
        );
    }

    public static void storeItemInSlot(int setIndex, int slotId, ItemStack itemStack, String itemUid) {
        if (itemStack != null && !itemStack.isEmpty()) {
            ExtraInventoryItem storedItem = new ExtraInventoryItem(Utils.getMineboxItemId(itemStack), itemStack.getFormattedName().getString(), itemUid);
            AutoConfig.getConfigHolder(ModConfig.class).getConfig().setItemInSlot(setIndex, slotId, storedItem);
            AutoConfig.getConfigHolder(ModConfig.class).save();
        }
    }

    public static ExtraInventoryItem getStoredItem(int setIndex, int slotId) {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig().getItemInSlot(setIndex, slotId);
    }

    public static void setSetName(int setIndex, String name) {
        AutoConfig.getConfigHolder(ModConfig.class).getConfig().setSetName(setIndex, name);
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static String getSetName(int setIndex) {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig().getSetName(setIndex);
    }

    public static void equipSet(DefaultedList<Slot> inventorySlots, int setIndex) {

        MinecraftClient client = MinecraftClient.getInstance();

        String setName = getSetName(setIndex);
        List<String> missingItems = new ArrayList<>();
        boolean foundCurrentItem = false;

        for (Map.Entry<Integer, ExtraInventoryItem> entry : AutoConfig.getConfigHolder(ModConfig.class).getConfig().getSet(setIndex).entrySet()) {
            int slotId = entry.getKey();
            ExtraInventoryItem storedItem = entry.getValue();
            String storedItemUuid = storedItem.itemUid;

            for (Slot slot : inventorySlots) {

                // Only loop in the player inventory slots
                if (slot.id < 46 || slot.id > 89) continue;

                if (!slot.hasStack()) continue;

                ItemStack slotItemStack = slot.getStack();
                if (!Utils.isMineboxItem(slotItemStack)) continue;

                String inventoryItemUuid = Utils.getMineboxItemUid(slotItemStack);
                if (inventoryItemUuid == null) continue;

                if (Objects.equals(storedItemUuid, inventoryItemUuid)) {
                    ScreenHandler screenHandler = client.player.currentScreenHandler;
                    client.interactionManager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(screenHandler.syncId, slotId, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(screenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, client.player);
                    foundCurrentItem = true;
                }
            }

            if (!foundCurrentItem) {
                missingItems.add("[" + storedItem.itemName + "]");
            } else {
                foundCurrentItem = false;
            }
        }

        Text returnMessage = null;

        if (missingItems.isEmpty()) {
            returnMessage = Text.literal("✔ You have equipped your " + setName + " set successfully ! ")
                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true));
        } else {
            returnMessage = Text.literal("❌ You have equipped your " + setName + " set but the following items were missing : " + missingItems.stream().map(Object::toString).collect(Collectors.joining(", ")))
                    .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true));
        }

        client.player.sendMessage(returnMessage, false);

    }

    public static void saveCurrentSetToSlotId(DefaultedList<Slot> inventorySlots, int setId) {
        AutoConfig.getConfigHolder(ModConfig.class).getConfig().clearSet(setId);
        for (Map.Entry<Integer, String> entry : SLOT_CATEGORY_MAP.entrySet()) {
            int slotId = entry.getKey();

            Slot slot = findSlotById(inventorySlots, slotId);
            if (slot != null && slot.hasStack() && Utils.isMineboxItem(slot.getStack())) {
                String itemUid = Utils.getMineboxItemUid(slot.getStack());
                if (itemUid != null) {
                    storeItemInSlot(setId, slotId, slot.getStack(), itemUid);
                }
            }
        }

        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    private static Slot findSlotById(DefaultedList<Slot> inventorySlots, int slotId) {
        for (Slot slot : inventorySlots) {
            if (slot.id == slotId) {
                return slot;
            }
        }
        return null;
    }

}
