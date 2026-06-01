package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.network.chat.Component;

@ConfigObject
public class HaversackHud implements Translatable {
    @ConfigEntry(id = "rate", translation = "mineboxadditions.config.huds.haversack.rate")
    @Comment(value = "", translation = "mineboxadditions.config.huds.haversack.rate.desc")
    public boolean rate = true;

    @ConfigEntry(id = "full", translation = "mineboxadditions.config.huds.haversack.full")
    @Comment(value = "", translation = "mineboxadditions.config.huds.haversack.full.desc")
    public boolean full = true;

    @Override
    public String getTranslationKey() {
        return Component.translatable("mineboxadditions.config.huds.haversack").getString();
    }
}
