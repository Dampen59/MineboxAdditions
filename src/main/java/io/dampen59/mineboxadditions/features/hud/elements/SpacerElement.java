package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.gui.DrawContext;

public class SpacerElement extends Element {
    private int size;

    public SpacerElement(int size) {
        setSize(size);
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public int getHeight() {
        return size;
    }

    @Override
    public void draw(DrawContext context, int x, int y) {}
}
