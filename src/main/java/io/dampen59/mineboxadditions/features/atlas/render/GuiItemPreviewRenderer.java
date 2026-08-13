package io.dampen59.mineboxadditions.features.atlas.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class GuiItemPreviewRenderer extends PictureInPictureRenderer<GuiItemPreviewRenderState> {

    @Override
    public Class<GuiItemPreviewRenderState> getRenderStateClass() {
        return GuiItemPreviewRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiItemPreviewRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        TrackingItemStackRenderState itemStackRenderState = state.itemStackRenderState();
        boolean flat = !itemStackRenderState.usesBlockLight();
        Minecraft.getInstance().gameRenderer.lighting().setupFor(flat ? Lighting.Entry.ITEMS_FLAT : Lighting.Entry.ITEMS_3D);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(state.rotationX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotationY()));
        itemStackRenderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "mineboxadditions_item_preview";
    }
}
