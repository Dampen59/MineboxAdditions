package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.features.atlas.render.GuiItemPreviewRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"
            ),
            index = 2
    )
    private List<PictureInPictureRenderer<?>> mbx$registerItemPreviewRenderer(List<PictureInPictureRenderer<?>> renderers) {
        List<PictureInPictureRenderer<?>> combined = new ArrayList<>(renderers);
        combined.add(new GuiItemPreviewRenderer());
        return combined;
    }
}
