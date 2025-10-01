package io.dampen59.mineboxadditions.features.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

public class ItemDurability {
    public record Durability(int current, int max, int damage) {}

    public static Optional<Durability> getDurability(ItemStack item) {
        String[] parts = getDurabilityParts(item);
        if (parts.length != 2) return Optional.empty();
        try {
            int current = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            int damage = max - current;
            return Optional.of(new Durability(current, max, damage));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static boolean hasDurability(ItemStack item) {
        String[] parts = getDurabilityParts(item);
        return parts.length == 2;
    }

    public static Integer getDurabilityStep(ItemStack item) {
        return getDurability(item)
                .map(d -> {
                    return MathHelper.clamp(Math.round(13.0F - (d.damage()) * 13.0F / d.max()), 0, 13);
                })
                .orElse(-1);
    }

    public static Integer getDurabilityColor(ItemStack item) {
        return getDurability(item)
                .map(d -> {
                    float f = Math.max(0.0F, ((float)d.max() - (float)d.damage()) / (float)d.max());
                    return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
                })
                .orElse(-1);
    }

    @Unique
    private static String[] getDurabilityParts(ItemStack item) {
        NbtComponent customData = item.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return new String[0];

        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains("mbitems:id")) return new String[0];

        String id = nbt.getString("mbitems:id").orElse(null);
        if (id == null) return new String[0];

        LoreComponent lore = item.get(DataComponentTypes.LORE);
        if (lore == null) return new String[0];

        for (Text line : lore.lines()) {
            if (!(line.getContent() instanceof TranslatableTextContent content)) continue;
            if (content.getKey().contains("mbx.durability") ||
                    content.getKey().contains("mbx.items.infinite_bag.amount_inside"))
                return content.getArg(0).getString().split("/");
        }
        return new String[0];
    }
}