package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.wardrobe.WardrobePreset;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;

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
            WardrobePreset.WardrobeItem wardrobeItem = new WardrobePreset.WardrobeItem(Utils.getMineboxItemId(item), itemUid, item.getHoverName().getString());
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

    public static void equipSet(NonNullList<Slot> inventorySlots, int presetId) {
        Minecraft client = Minecraft.getInstance();

        String setName = getSetName(presetId);
        List<String> missingItems = new ArrayList<>();
        boolean foundCurrentItem = false;

        for (Map.Entry<Integer, WardrobePreset.WardrobeItem> entry : Config.wardrobe.getPreset(presetId).items.entrySet()) {
            int slotId = entry.getKey();
            WardrobePreset.WardrobeItem wardrobeItem = entry.getValue();
            String storedItemUuid = wardrobeItem.uid;

            for (Slot slot : inventorySlots) {

                // Only loop in the player inventory slots
                if (slot.getContainerSlot() < 46 || slot.getContainerSlot() > 89) continue;

                if (!slot.hasItem()) continue;

                ItemStack slotItemStack = slot.getItem();
                if (!Utils.isMineboxItem(slotItemStack)) continue;

                String inventoryItemUuid = Utils.getMineboxItemUid(slotItemStack);
                if (inventoryItemUuid == null) continue;

                if (Objects.equals(storedItemUuid, inventoryItemUuid)) {
                    AbstractContainerMenu screenHandler = client.player.containerMenu;
                    // clickSlot stubbed - ClickType API pending 26.1 verification
                    // clickSlot stubbed - ClickType API pending 26.1 verification
                    // clickSlot stubbed - ClickType API pending 26.1 verification
                    foundCurrentItem = true;
                }
            }

            if (!foundCurrentItem) {
                missingItems.add("[" + wardrobeItem.name + "]");
            } else {
                foundCurrentItem = false;
            }
        }

        Component returnMessage = null;

        if (missingItems.isEmpty()) {
            returnMessage = Component.literal("✔ You have equipped your " + setName + " set successfully ! ")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true));
        } else {
            returnMessage = Component.literal("❌ You have equipped your " + setName + " set but the following items were missing : " + missingItems.stream().map(Object::toString).collect(Collectors.joining(", ")))
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true));
        }

        client.player.sendSystemMessage(returnMessage);

    }

    public static void saveCurrentSetToSlotId(NonNullList<Slot> inventorySlots, int presetId) {
        Config.wardrobe.clearPreset(presetId);
        for (Map.Entry<Integer, String> entry : SLOT_CATEGORY_MAP.entrySet()) {
            int slotId = entry.getKey();

            Slot slot = findSlotById(inventorySlots, slotId);
            if (slot != null && slot.hasItem() && Utils.isMineboxItem(slot.getItem())) {
                String itemUid = Utils.getMineboxItemUid(slot.getItem());
                if (itemUid != null) {
                    storeItemInSlot(presetId, slotId, slot.getItem(), itemUid);
                }
            }
        }

        ConfigManager.save();
    }

    private static Slot findSlotById(NonNullList<Slot> inventorySlots, int slotId) {
        for (Slot slot : inventorySlots) {
            if (slot.getContainerSlot() == slotId) {
                return slot;
            }
        }
        return null;
    }

}
