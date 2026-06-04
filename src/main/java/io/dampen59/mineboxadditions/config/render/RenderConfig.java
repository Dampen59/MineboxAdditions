package io.dampen59.mineboxadditions.config.render;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import io.dampen59.mineboxadditions.config.render.categories.FishingShoals;

@Category(value = "render", categories = {
        FishingShoals.class
})
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.render",
        descriptionTranslation = "mineboxadditions.config.render.desc"
)
public class RenderConfig {
}
