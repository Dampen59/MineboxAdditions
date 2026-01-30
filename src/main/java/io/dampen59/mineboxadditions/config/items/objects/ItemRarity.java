package io.dampen59.mineboxadditions.config.items.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.text.Text;

@ConfigObject
public class ItemRarity implements Translatable {
    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.items.rarity.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.items.rarity.enabled.desc")
    public boolean enabled = false;

    public static enum Mode {
        FILL,
        CIRCLE
    }
    @ConfigEntry(id = "mode", translation = "mineboxadditions.config.items.rarity.mode")
    @Comment(value = "", translation = "mineboxadditions.config.items.rarity.mode.desc")
    public Mode mode = Mode.FILL;

    @ConfigEntry(id = "opacity", translation = "mineboxadditions.config.items.rarity.opacity")
    @Comment(value = "", translation = "mineboxadditions.config.items.rarity.opacity.desc")
    @ConfigOption.Slider()
    @ConfigOption.Range(min = 1, max = 100)
    public int opacity = 50;

    @Override
    public String getTranslationKey() {
        return Text.translatable("mineboxadditions.config.items.rarity").getString();
    }
}
