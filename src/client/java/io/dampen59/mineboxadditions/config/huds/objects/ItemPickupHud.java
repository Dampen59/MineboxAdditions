package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;

@ConfigObject
public class ItemPickupHud implements Translatable {
    @ConfigEntry(id = "enabled")
    @Comment(value = "")
    public boolean enabled = true;

    @ConfigEntry(id = "count")
    @Comment(value = "")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 10)
    public int count = 5;

    @ConfigEntry(id = "duration")
    @Comment(value = "")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 10)
    public int duration = 2;

    @ConfigEntry(id = "merge")
    @Comment(value = "")
    public boolean merge = true;

    @Override
    public String getTranslationKey() {
        return "Edit Item Pickup Hud";
    }
}
