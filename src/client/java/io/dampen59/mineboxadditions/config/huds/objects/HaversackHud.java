package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;

@ConfigObject
public class HaversackHud implements Translatable {
    @ConfigEntry(id = "rate")
    @Comment(value = "")
    public boolean rate = true;

    @ConfigEntry(id = "full")
    @Comment(value = "")
    public boolean full = true;

    @Override
    public String getTranslationKey() {
        return "Edit Haversack Hud";
    }
}
