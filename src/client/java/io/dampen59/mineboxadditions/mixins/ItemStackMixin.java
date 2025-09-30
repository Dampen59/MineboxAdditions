package io.dampen59.mineboxadditions.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.dampen59.mineboxadditions.features.item.ItemDurability;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyReturnValue(method = "isItemBarVisible", at = @At("RETURN"))
    private boolean mba$isItemBarVisible(boolean original) {
        ItemStack item = (ItemStack)(Object)this;
        if (ItemDurability.hasDurability(item)) return true;
        return original;
    }

    @ModifyReturnValue(method = "getItemBarStep", at = @At("RETURN"))
    private int mba$getItemBarStep(int original) {
        ItemStack item = (ItemStack)(Object)this;
        int step = ItemDurability.getDurabilityStep(item);
        if (step >= 0) return step;
        return original;
    }

    @ModifyReturnValue(method = "getItemBarColor", at = @At("RETURN"))
    private int mba$getItemBarColor(int original) {
        ItemStack item = (ItemStack)(Object)this;
        int color = ItemDurability.getDurabilityColor(item);
        if (color >= 0) return color;
        return original;
    }
}
