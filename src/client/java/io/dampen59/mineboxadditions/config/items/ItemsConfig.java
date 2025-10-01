package io.dampen59.mineboxadditions.config.items;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import io.dampen59.mineboxadditions.config.items.objects.ItemRarity;

@Category(value = "items")
public class ItemsConfig {
    @ConfigEntry(id = "rarity")
    @Comment(value = "")
    public static final ItemRarity rarity = new ItemRarity();

    @ConfigEntry(id = "museumIndicator")
    public static boolean museumIndicator = true;
}
