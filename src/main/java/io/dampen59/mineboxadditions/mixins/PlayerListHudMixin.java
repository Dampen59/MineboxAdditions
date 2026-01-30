package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "setFooter", at = @At("HEAD"))
    private void mbx$setFooter(Text footer, CallbackInfo ci) {
        Utils.updateLocation(footer);
    }
}
