package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.text.Text;

@ConfigObject
public class ItemPickupHud implements Translatable {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.huds.itempickup.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.huds.itempickup.enabled.desc")
    public boolean enabled = true;

    @ConfigEntry(id = "count", translation = "mineboxadditions.config.huds.itempickup.count")
    @Comment(value = "", translation = "mineboxadditions.config.huds.itempickup.count.desc")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 10)
    public int count = 5;

    @ConfigEntry(id = "duration", translation = "mineboxadditions.config.huds.itempickup.duration")
    @Comment(value = "", translation = "mineboxadditions.config.huds.itempickup.duration.desc")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 10)
    public int duration = 2;

    @ConfigEntry(id = "merge", translation = "mineboxadditions.config.huds.itempickup.merge")
    @Comment(value = "", translation = "mineboxadditions.config.huds.itempickup.merge.desc")
    public boolean merge = true;

    @Override
    public String getTranslationKey() {
        return Text.translatable("mineboxadditions.config.huds.itempickup").getString();
    }
}
