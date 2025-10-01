package io.dampen59.mineboxadditions.config.huds.categories;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;

@Category("fishingdrops")
public class FishingDrops {
    @ConfigEntry(id = "enabled")
    public static boolean enabled = true;

    @ConfigEntry(id = "renderRadius")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 50)
    public static int renderRadius = 25;
}
