package io.dampen59.mineboxadditions;

import io.dampen59.mineboxadditions.minebox.ExtraInventoryItem;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.HashMap;
import java.util.Map;

@Config(name = "mineboxadditions")
public class ModConfig implements ConfigData {

    public ModConfig() {
        for (int i = 0; i < 4; i++) {
            storedItemSets.put(i, new HashMap<>());
            setNames.put(i, "Set " + (i + 1));
        }
    }

    public enum RaritiesDisplayMode {
        FILL,
        CIRCLE
    }

    @ConfigEntry.Gui.Excluded
    public String socketServerAddress = "http://mbxadditions.dampen59.io:3000";

    @ConfigEntry.Gui.CollapsibleObject
    public ShopsAlertsSettings shopsAlertsSettings = new ShopsAlertsSettings();
    public static class ShopsAlertsSettings {
        public boolean getMouseAlerts = true;
        public boolean getBakeryAlerts = true;
        public boolean getBuckstarAlerts = true;
        public boolean getCocktailAlerts = true;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public DurabilitySettings durabilitySettings = new DurabilitySettings();
    public static class DurabilitySettings {
        public boolean haversackDurability = false;
        public boolean harvesterDurability = false;
    }

    public static class ItemPickupSettings {
        public boolean displayItemsPickups = false;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int maxPickupNotifications = 5;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int pickupNotificationDuration = 2;
        public boolean mergeLines = true;
    }

    public static class ItemRaritySettings {
        public boolean displayItemsRarity = false;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
        public RaritiesDisplayMode displayMode = RaritiesDisplayMode.FILL;

        @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
        public int backgroundOpacity = 50;
    }

    public static class FishingSettings {
        public boolean showFishDrops = true;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 50)
        public int fishDropRadius = 25;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public DisplaySettings displaySettings = new DisplaySettings();
    public static class DisplaySettings {
        @ConfigEntry.Gui.CollapsibleObject
        public ItemPickupSettings itemPickupSettings = new ItemPickupSettings();

        @ConfigEntry.Gui.CollapsibleObject
        public ItemRaritySettings itemRaritySettings = new ItemRaritySettings();

        @ConfigEntry.Gui.CollapsibleObject
        public FishingSettings fishingSettings = new FishingSettings();

        public boolean displayFullMoon = false;
    }

    public static class MultiChannelSettings {
        public boolean enableMultiChannel = true;

        public boolean enableFrench = true;
        public boolean enableEnglish = true;
        public boolean enableSpanish = false;
        public boolean enableRussian = false;
        public boolean enablePortuguese = false;
        public boolean enableDeutsch = false;
        public boolean enableChinese = false;
        public boolean enablePolish = false;
        public boolean enableItalian = false;
        public boolean enableJapanese = false;
        public boolean enableDutch = false;
        public boolean enableTurkish = false;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public ChatSettings chatSettings = new ChatSettings();
    public static class ChatSettings {

        @ConfigEntry.Gui.CollapsibleObject
        public MultiChannelSettings multiChannelSettings = new MultiChannelSettings();

    }

    public boolean autoIslandOnLogin = false;
    public boolean bankMacro = true;

    // Extra inventory related things
    @ConfigEntry.Gui.Excluded
    public Map<Integer, Map<Integer, ExtraInventoryItem>> storedItemSets = new HashMap<>();

    @ConfigEntry.Gui.Excluded
    public Map<Integer, String> setNames = new HashMap<>();

    public void setItemInSlot(int setIndex, int slotId, ExtraInventoryItem item) {
        if (setIndex >= 0 && setIndex < 4) {
            storedItemSets.get(setIndex).put(slotId, item);
        }
    }

    public ExtraInventoryItem getItemInSlot(int setIndex, int slotId) {
        if (setIndex >= 0 && setIndex < 4) {
            return storedItemSets.get(setIndex).get(slotId);
        }
        return null;
    }

    public Map<Integer, ExtraInventoryItem> getSet(int setIndex) {
        return storedItemSets.getOrDefault(setIndex, new HashMap<>());
    }

    public void setSetName(int setIndex, String name) {
        if (setIndex >= 0 && setIndex < 4) {
            setNames.put(setIndex, name);
        }
    }

    public String getSetName(int setIndex) {
        return setNames.getOrDefault(setIndex, "Set " + (setIndex + 1));
    }
}
