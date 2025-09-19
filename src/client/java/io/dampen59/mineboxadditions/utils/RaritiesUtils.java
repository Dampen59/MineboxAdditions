package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.awt.*;
import java.util.Map;

public class RaritiesUtils {
    public static final Map<String, Integer> RARITY_ARGB = Map.of(
            "common",    0xFF665466,
            "uncommon",  0xFF00C06F,
            "rare",      0xFF00A5FC,
            "epic",      0xFFF816FC,
            "legendary", 0xFFFFBE35,
            "mythic",    0xFFA0060A
    );

    public static int percentToAlpha(double p) {
        return (int) Math.round((255.0 / 100.0) * p);
    }

    public static Color adjustAlpha(Color color, double percentage) {
        int newAlpha = percentToAlpha(percentage);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    public static Color getRarityColor(String rarity) {
        if (rarity == null) return null;

        Integer base = RARITY_ARGB.get(rarity.toLowerCase());
        if (base == null) return null;

        double opacityCfg = AutoConfig.getConfigHolder(ModConfig.class)
                .getConfig().displaySettings.itemRaritySettings.backgroundOpacity;

        double opacity = opacityCfg > 1.0 ? (opacityCfg / 100.0) : opacityCfg;
        opacity = Math.max(0.0, Math.min(1.0, opacity));

        int a = (int) Math.round(255.0 * opacity);
        int rgb  = base & 0x00FFFFFF;
        int argb = (a << 24) | rgb;

        return new Color(argb, true);
    }


    public static Color getItemRarityColorFromLore(ItemStack itemStack) {
        LoreComponent loreComponent = itemStack.get(DataComponentTypes.LORE);
        if (loreComponent == null) return null;

        final String PREFIX = "mbx.rarities.";
        final String SUFFIX = ".icon";

        for (Text loreLine : loreComponent.lines()) {
            String key = loreLine.getContent() instanceof TranslatableTextContent translatable
                    ? translatable.getKey()
                    : loreLine.getString();
            if (key.startsWith(PREFIX) && key.endsWith(SUFFIX)) {
                String rarity = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
                return getRarityColor(rarity);
            }
        }
        return null;
    }
}
