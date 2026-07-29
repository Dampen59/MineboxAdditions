package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Element {
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract void draw(GuiGraphicsExtractor context, int x, int y);
    public int width() { return getWidth(); }
}