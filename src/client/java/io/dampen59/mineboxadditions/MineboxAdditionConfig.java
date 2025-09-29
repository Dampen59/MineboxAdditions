package io.dampen59.mineboxadditions;

import io.dampen59.mineboxadditions.minebox.ExtraInventoryItem;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.HashMap;
import java.util.Map;

@Config(name = "mineboxadditions")
public class MineboxAdditionConfig implements ConfigData {

    public MineboxAdditionConfig() {
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

    @ConfigEntry.Gui.Excluded
    public String selectedMicName = "";

    @ConfigEntry.Gui.Excluded
    public String selectedSpeakerName = "";

    @ConfigEntry.Gui.Excluded
    public float micGainDb = 0.0f;

    @ConfigEntry.Gui.Excluded
    public float speakerVolumeMultiplier = 1.0f;

    @ConfigEntry.Gui.Excluded
    public int rainHudX = 4;

    @ConfigEntry.Gui.Excluded
    public int rainHudY = 36;

    @ConfigEntry.Gui.Excluded
    public int stormHudX = 68;

    @ConfigEntry.Gui.Excluded
    public int stormHudY = 36;

    @ConfigEntry.Gui.Excluded
    public int shopHudX = 4;

    @ConfigEntry.Gui.Excluded
    public int shopHudY = 4;

    @ConfigEntry.Gui.Excluded
    public int fullMoonHudX = 132;

    @ConfigEntry.Gui.Excluded
    public int fullMoonHudY = 36;

    @ConfigEntry.Gui.Excluded
    public int haverSackFillRateX = 4;

    @ConfigEntry.Gui.Excluded
    public int haverSackFillRateY = 52;

    @ConfigEntry.Gui.Excluded
    public int haversackFullInX = 4;

    @ConfigEntry.Gui.Excluded
    public int haversackFullInY = 68;

    @ConfigEntry.Gui.Excluded
    public int mermaidRequestHudX = 4;

    @ConfigEntry.Gui.Excluded
    public int getMermaidRequestHudY = 20;

    @ConfigEntry.Gui.Excluded
    public int itemPickupHudX = -50;

    @ConfigEntry.Gui.Excluded
    public int itemPickupHudY = 4;

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

        public boolean displayItemRange = false;
        public boolean displayMuseumMissingItems = true;
        public boolean displayFullMoon = false;
        public boolean displayNextRain = true;
        public boolean displayNextStorm = true;
        public boolean displayHaversackFillRate = true;
        public boolean displayHaversackFullIn = true;
        public boolean displayMermaidRequest = true;
    }

    public boolean autoIslandOnLogin = false;

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

    public void clearSet(int setIndex) {
        if (setIndex >= 0 && setIndex < 4) {
            Map<Integer, ExtraInventoryItem> set = storedItemSets.get(setIndex);
            if (set != null) {
                set.clear();
            } else {
                storedItemSets.put(setIndex, new HashMap<>());
            }
        }
    }

    public void setSetName(int setIndex, String name) {
        if (setIndex >= 0 && setIndex < 4) {
            setNames.put(setIndex, name);
        }
    }

    public String getSetName(int setIndex) {
        return setNames.getOrDefault(setIndex, "Set " + (setIndex + 1));
    }

    @ConfigEntry.Gui.Excluded
    public Map<String, HarvestablesPrefs> harvestablesPrefs = new HashMap<>();

    public static class HarvestablesPrefs {
        public Map<String, Boolean> categoryEnabled = new HashMap<>();
        public Map<String, Map<String, Boolean>> itemEnabled = new HashMap<>();
        public Map<String, Map<String, Integer>> itemColor = new HashMap<>();
    }

}
