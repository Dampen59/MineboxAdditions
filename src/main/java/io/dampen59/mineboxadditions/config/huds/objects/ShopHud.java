package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.network.chat.Component;

@ConfigObject
public class ShopHud implements Translatable {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.huds.shop.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.enabled.desc")
    public boolean enabled = true;

    @Override
    public String getTranslationKey() {
        return Component.translatable("mineboxadditions.config.huds.shop").getString();
    }
}
