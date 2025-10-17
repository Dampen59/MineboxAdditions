package io.dampen59.mineboxadditions.config;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.config.other.HarvestablesSettings;
import io.dampen59.mineboxadditions.config.other.WardrobePresets;

@ConfigInfo.Provider(ConfigInfoProvider.class)
@com.teamresourceful.resourcefulconfig.api.annotations.Config(
        value = MineboxAdditions.NAMESPACE,
        version = 1,
        categories = {
                HudsConfig.class,
                ItemsConfig.class
        }
)
public class Config {
        @ConfigOption.Hidden
        @ConfigEntry(id = "socketServerAddress")
        public static String socketServerAddress = "http://mbxadditions.dampen59.io:3000";

        @ConfigOption.Hidden
        @ConfigEntry(id = "selectedMicName")
        public static String selectedMicName = "";

        @ConfigOption.Hidden
        @ConfigEntry(id = "selectedSpeakerName")
        public static String selectedSpeakerName = "";

        @ConfigOption.Hidden
        @ConfigEntry(id = "micGainDb")
        public static float micGainDb = 0.0f;

        @ConfigOption.Hidden
        @ConfigEntry(id = "speakerVolumeMultiplier")
        public static float speakerVolumeMultiplier = 1.0f;

        @ConfigOption.Hidden
        @ConfigEntry(id = "wardrobe")
        public static final WardrobePresets wardrobe = new WardrobePresets();

        @ConfigOption.Hidden
        @ConfigEntry(id = "harvestables")
        public static final HarvestablesSettings harvestables = new HarvestablesSettings();

        @ConfigEntry(id = "autoIsland", translation = "mineboxadditions.config.autoIsland")
        @Comment(value = "", translation = "mineboxadditions.config.autoIsland.desc")
        public static boolean autoIsland = false;

        @ConfigEntry(id = "shinyNotify", translation = "mineboxadditions.config.shinyNotify")
        @Comment(value = "", translation = "mineboxadditions.config.shinyNotify.desc")
        public static ShinyNotify shinyNotify = ShinyNotify.MANUAL;
        public enum ShinyNotify {
                AUTO,
                MANUAL,
                OFF
        }
}