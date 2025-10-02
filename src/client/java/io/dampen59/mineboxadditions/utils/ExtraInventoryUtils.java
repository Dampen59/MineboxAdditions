package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.wardrobe.WardrobePreset;
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

    public static void storeItemInSlot(int presetId, int slotId, ItemStack item, String itemUid) {
        if (item != null && !item.isEmpty()) {
            WardrobePreset.WardrobeItem wardrobeItem = new WardrobePreset.WardrobeItem(Utils.getMineboxItemId(item), itemUid, item.getFormattedName().getString());
            Config.wardrobe.setPresetItem(presetId, slotId, wardrobeItem);
            ConfigManager.save();
        }
    }

    public static void setSetName(int presetId, String name) {
        WardrobePreset preset = Config.wardrobe.getPreset(presetId);
        preset.name = name;
        ConfigManager.save();
    }

    public static String getSetName(int presetId) {
        WardrobePreset preset = Config.wardrobe.getPreset(presetId);
        if (preset.name == null) return "Set " + (presetId + 1);
        return preset.name;
    }

    public static void equipSet(DefaultedList<Slot> inventorySlots, int presetId) {
        MinecraftClient client = MinecraftClient.getInstance();

        String setName = getSetName(presetId);
        List<String> missingItems = new ArrayList<>();
        boolean foundCurrentItem = false;

        for (Map.Entry<Integer, WardrobePreset.WardrobeItem> entry : Config.wardrobe.getPreset(presetId).items.entrySet()) {
            int slotId = entry.getKey();
            WardrobePreset.WardrobeItem wardrobeItem = entry.getValue();
            String storedItemUuid = wardrobeItem.uid;

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
                missingItems.add("[" + wardrobeItem.name + "]");
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

    public static void saveCurrentSetToSlotId(DefaultedList<Slot> inventorySlots, int presetId) {
        Config.wardrobe.clearPreset(presetId);
        for (Map.Entry<Integer, String> entry : SLOT_CATEGORY_MAP.entrySet()) {
            int slotId = entry.getKey();

            Slot slot = findSlotById(inventorySlots, slotId);
            if (slot != null && slot.hasStack() && Utils.isMineboxItem(slot.getStack())) {
                String itemUid = Utils.getMineboxItemUid(slot.getStack());
                if (itemUid != null) {
                    storeItemInSlot(presetId, slotId, slot.getStack(), itemUid);
                }
            }
        }

        ConfigManager.save();
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
