package io.dampen59.mineboxadditions.config.items.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;

@ConfigObject
public class ItemRarity implements Translatable {
    @ConfigEntry(id = "enabled")
    public boolean enabled = false;

    public static enum Mode {
        FILL,
        CIRCLE
    }
    @ConfigEntry(id = "mode")
    public Mode mode = Mode.FILL;

    @ConfigEntry(id = "opacity")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 100)
    public int opacity = 50;

    @Override
    public String getTranslationKey() {
        return "Edit Item Rarity";
    }
}
