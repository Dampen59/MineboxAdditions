package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "setFooter", at = @At("HEAD"))
    private void mbx$setFooter(Component footer, CallbackInfo ci) {
        Utils.updateLocation(footer);
    }
}
