package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;

@ConfigObject
public class ShopHud implements Translatable {
    @ConfigEntry(id = "enabled")
    @Comment(value = "")
    public boolean enabled = true;

    @ConfigEntry(id = "mouse")
    @Comment(value = "")
    public boolean mouse = true;

    @ConfigEntry(id = "bakery")
    @Comment(value = "")
    public boolean bakery = true;

    @ConfigEntry(id = "buckstar")
    @Comment(value = "")
    public boolean buckstar = true;

    @ConfigEntry(id = "sharkoffe")
    @Comment(value = "")
    public boolean sharkoffe = true;

    @Override
    public String getTranslationKey() {
        return "Edit Shop Hud";
    }
}