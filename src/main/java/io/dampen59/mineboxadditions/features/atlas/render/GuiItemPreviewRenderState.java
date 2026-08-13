package io.dampen59.mineboxadditions.features.atlas.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;

public record GuiItemPreviewRenderState(
        TrackingItemStackRenderState itemStackRenderState,
        float rotationX,
        float rotationY,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public GuiItemPreviewRenderState(
            TrackingItemStackRenderState itemStackRenderState,
            float rotationX,
            float rotationY,
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(itemStackRenderState, rotationX, rotationY, x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
