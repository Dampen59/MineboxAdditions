package io.dampen59.mineboxadditions.config.items;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import io.dampen59.mineboxadditions.config.items.objects.ItemRarity;

@Category(value = "items")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.items",
        descriptionTranslation = "mineboxadditions.config.items.desc"
)
public class ItemsConfig {
    @ConfigEntry(id = "rarity", translation = "mineboxadditions.config.items.rarity")
    @Comment(value = "", translation = "mineboxadditions.config.items.rarity.desc")
    public static final ItemRarity rarity = new ItemRarity();

    @ConfigEntry(id = "museumIndicator", translation = "mineboxadditions.config.items.museumIndicator")
    @Comment(value = "", translation = "mineboxadditions.config.items.museumIndicator.desc")
    public static boolean museumIndicator = true;

    @ConfigEntry(id = "rangeDisplay", translation = "mineboxadditions.config.items.rangeDisplay")
    @Comment(value = "", translation = "mineboxadditions.config.items.rangeDisplay.desc")
    public static boolean rangeDisplay = false;
}
