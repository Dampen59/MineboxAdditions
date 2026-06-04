package io.dampen59.mineboxadditions.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.features.item.ItemDurability;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @ModifyReturnValue(method = "isBarVisible", at = @At("RETURN"))
    private boolean mbx$isBarVisible(boolean original) {
        boolean toolBar = ItemsConfig.durabilityBar && ItemDurability.hasToolDurability(self());
        boolean fillBar = ItemsConfig.haversackFillBar && ItemDurability.hasHaversackFill(self());
        return toolBar || fillBar || original;
    }

    @ModifyReturnValue(method = "getBarWidth", at = @At("RETURN"))
    private int mbx$getBarWidth(int original) {
        int step = ItemDurability.getDurabilityStep(self());
        return step >= 0 ? step : original;
    }

    @ModifyReturnValue(method = "getBarColor", at = @At("RETURN"))
    private int mbx$getBarColor(int original) {
        int color = ItemDurability.getDurabilityColor(self());
        return color >= 0 ? color : original;
    }
}
