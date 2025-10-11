package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.text.Text;

@ConfigObject
public class ShopHud implements Translatable {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.huds.shop.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.enabled.desc")
    public boolean enabled = true;

    @ConfigOption.Separator(value = "Alerts")
    @ConfigEntry(id = "mouse", translation = "mineboxadditions.config.huds.shop.mouse")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.mouse.desc")
    public boolean mouse = true;

    @ConfigEntry(id = "bakery", translation = "mineboxadditions.config.huds.shop.bakery")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.bakery.desc")
    public boolean bakery = true;

    @ConfigEntry(id = "buckstar", translation = "mineboxadditions.config.huds.shop.buckstar")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.buckstar.desc")
    public boolean buckstar = true;

    @ConfigEntry(id = "sharkoffe", translation = "mineboxadditions.config.huds.shop.sharkoffe")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.sharkoffe.desc")
    public boolean sharkoffe = true;

    @Override
    public String getTranslationKey() {
        return Text.translatable("mineboxadditions.config.huds.shop").getString();
    }
}