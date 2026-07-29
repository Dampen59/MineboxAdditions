package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class TextureElement extends Element {
    private Identifier texture;
    private int width;
    private int height;

    public TextureElement(Identifier texture, int width, int height) {
        setTexture(texture, width, height);
    }

    public void setTexture(Identifier texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphicsExtractor context, int x, int y) {
        context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    }
}
