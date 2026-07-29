package io.dampen59.mineboxadditions.config.spells;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;

@Category(value = "spells")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.spells",
        descriptionTranslation = "mineboxadditions.config.spells.desc"
)
public class SpellsConfig {
    @ConfigEntry(id = "cooldownMessages", translation = "mineboxadditions.config.spells.cooldownMessages")
    @Comment(value = "", translation = "mineboxadditions.config.spells.cooldownMessages.desc")
    public static boolean cooldownMessages = true;
}
