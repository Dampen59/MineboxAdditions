package io.dampen59.mineboxadditions.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyReturnValue(method = "isItemBarVisible", at = @At("RETURN"))
    private boolean mba$isItemBarVisible(boolean original) {
        ItemStack item = (ItemStack)(Object)this;
        String[] parts = getDurabilityParts(item);
        if (parts.length == 2) return true;
        return original;
    }

    @ModifyReturnValue(method = "getItemBarStep", at = @At("RETURN"))
    private int mba$getItemBarStep(int original) {
        ItemStack item = (ItemStack)(Object)this;
        String[] parts = getDurabilityParts(item);

        if (parts.length == 2) {
            int current = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            int damage = max - current;
            return MathHelper.clamp(Math.round(13.0F - (float)damage * 13.0F / (float)max), 0, 13);
        }
        return original;
    }

    @ModifyReturnValue(method = "getItemBarColor", at = @At("RETURN"))
    private int mba$getItemBarColor(int original) {
        ItemStack item = (ItemStack)(Object)this;
        String[] parts = getDurabilityParts(item);
        if (parts.length == 2) {
            int current = Integer.parseInt(parts[0]);
            int max = Integer.parseInt(parts[1]);
            int damage = max - current;
            float f = Math.max(0.0F, ((float)max - (float)damage) / (float)max);
            return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
        }
        return original;
    }

    @Unique
    private String[] getDurabilityParts(ItemStack item) {
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
