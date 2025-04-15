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
    public static final Map<String, Color> BASE_RARITY_COLORS = Map.of(
            "common", new Color(0x66, 0x54, 0x66),
            "uncommon", new Color(0x00, 0xC0, 0x6F),
            "rare", new Color(0x00, 0xA5, 0xFC),
            "epic", new Color(0xF8, 0x16, 0xFC),
            "legendary", new Color(0xFF, 0xBE, 0x35),
            "mythic", new Color(0xA0, 0x06, 0x0A)
    );

    public static int percentToAlpha(double p) {
        return (int) Math.round((255.0 / 100.0) * p);
    }

    public static Color adjustAlpha(Color color, double percentage) {
        int newAlpha = percentToAlpha(percentage);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    public static Color getRarityColor(String rarity) {
        Color baseColor = BASE_RARITY_COLORS.get(rarity);
        if (baseColor == null) {
            return null;
        }

        double opacity = AutoConfig.getConfigHolder(ModConfig.class).getConfig().displaySettings.itemRaritySettings.backgroundOpacity;
        return adjustAlpha(baseColor, opacity);
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
