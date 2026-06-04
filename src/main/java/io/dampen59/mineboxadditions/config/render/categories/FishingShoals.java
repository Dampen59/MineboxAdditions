package io.dampen59.mineboxadditions.config.render.categories;

import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Category("fishingshoals")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.render.fishingshoals",
        descriptionTranslation = "mineboxadditions.config.render.fishingshoals.desc"
)
public class FishingShoals {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.render.fishingshoals.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.render.fishingshoals.enabled.desc")
    public static boolean enabled = true;

    @ConfigEntry(id = "renderRadius", translation = "mineboxadditions.config.render.fishingshoals.renderRadius")
    @Comment(value = "", translation = "mineboxadditions.config.render.fishingshoals.renderRadius.desc")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 50)
    public static int renderRadius = 25;
}
