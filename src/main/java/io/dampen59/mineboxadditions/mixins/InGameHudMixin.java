package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.huds.WeatherHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("TAIL")
    )
    private void mbx$render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        for (Hud hud : HudManager.INSTANCE.getAll()) {
            if (hud instanceof ItemPickupHud) continue;
            if (hud instanceof HaversackHud.RateHud) continue;
            if (hud instanceof HaversackHud.FullHud) continue;
            if (hud instanceof WeatherHud.FullMoonHud &&
                MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() != 0) continue;

            if (hud.getState()) hud.draw(context);
        }
    }
}
