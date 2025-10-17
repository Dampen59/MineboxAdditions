package io.dampen59.mineboxadditions.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.dampen59.mineboxadditions.features.item.ItemDurability;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @ModifyReturnValue(method = "isItemBarVisible", at = @At("RETURN"))
    private boolean mbx$isItemBarVisible(boolean original) {
        return ItemDurability.hasDurability(self()) || original;
    }

    @ModifyReturnValue(method = "getItemBarStep", at = @At("RETURN"))
    private int mbx$getItemBarStep(int original) {
        int step = ItemDurability.getDurabilityStep(self());
        return step >= 0 ? step : original;
    }

    @ModifyReturnValue(method = "getItemBarColor", at = @At("RETURN"))
    private int mbx$getItemBarColor(int original) {
        int color = ItemDurability.getDurabilityColor(self());
        return color >= 0 ? color : original;
    }
}
