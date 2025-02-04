package io.dampen59.mineboxadditions;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "mineboxadditions")
public class ModConfig implements ConfigData {

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

    @ConfigEntry.Gui.CollapsibleObject
    public NetworkFeatures networkFeatures = new NetworkFeatures();
    public static class NetworkFeatures {
        public boolean enableNetworkFeatures = false;
        public boolean receiveShopsAlerts = true;
        public boolean sendShopsAlerts = true;
    }

    public static class ItemPickupSettings {
        public boolean displayItemsPickups = false;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int maxPickupNotifications = 5;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int pickupNotificationDuration = 2;
        public boolean mergeLines = true;
    }

    @ConfigEntry.Gui.CollapsibleObject
    public DisplaySettings displaySettings = new DisplaySettings();
    public static class DisplaySettings {
        @ConfigEntry.Gui.CollapsibleObject
        public ItemPickupSettings itemPickupSettings = new ItemPickupSettings();
        public boolean displayFullMoon = false;
    }

    public boolean autoIslandOnLogin = false;
}
