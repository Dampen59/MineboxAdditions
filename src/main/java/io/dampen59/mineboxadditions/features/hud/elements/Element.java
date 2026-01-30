package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.gui.DrawContext;

public abstract class Element {
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract void draw(DrawContext context, int x, int y);
}