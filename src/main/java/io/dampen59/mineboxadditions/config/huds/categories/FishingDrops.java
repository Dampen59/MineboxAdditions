package io.dampen59.mineboxadditions.config.huds.categories;

import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Category("fishingdrops")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.huds.fishingdrops",
        descriptionTranslation = "mineboxadditions.config.huds.fishingdrops.desc"
)
public class FishingDrops {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.huds.fishingdrops.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.huds.fishingdrops.enabled.desc")
    public static boolean enabled = true;

    @ConfigEntry(id = "renderRadius", translation = "mineboxadditions.config.huds.fishingdrops.renderRadius")
    @Comment(value = "", translation = "mineboxadditions.config.huds.fishingdrops.renderRadius.desc")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 50)
    public static int renderRadius = 25;
}
