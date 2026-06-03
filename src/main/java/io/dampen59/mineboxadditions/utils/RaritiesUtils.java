package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.awt.*;
import java.util.Map;

public class RaritiesUtils {
    public static final Map<String, Integer> RARITY_ARGB = Map.of(
            "trash",     0xFFCBD5E1,
            "common",    0xFF665466,
            "uncommon",  0xFF00C06F,
            "rare",      0xFF00A5FC,
            "epic",      0xFFF816FC,
            "legendary", 0xFFFFBE35,
            "mythic",    0xFFA0060A,
            "prototype", 0xFF66FF00,
            "contraband",0xFF66FF00
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

        double opacityCfg = ItemsConfig.rarity.opacity;

        double opacity = opacityCfg > 1.0 ? (opacityCfg / 100.0) : opacityCfg;
        opacity = Math.max(0.0, Math.min(1.0, opacity));

        int a = (int) Math.round(255.0 * opacity);
        int rgb  = base & 0x00FFFFFF;
        int argb = (a << 24) | rgb;

        return new Color(argb, true);
    }


    public static Color getItemRarityColorFromLore(ItemStack itemStack) {
        ItemLore loreComponent = itemStack.get(DataComponents.LORE);
        if (loreComponent == null) return null;

        final String PREFIX = "mbx.rarities.";
        final String SUFFIX = ".icon";

        for (Component loreLine : loreComponent.lines()) {
            String key = loreLine.getContents() instanceof TranslatableContents translatable
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
