package io.dampen59.mineboxadditions.features.hud.elements.stack;

import io.dampen59.mineboxadditions.features.hud.elements.Element;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import net.minecraft.client.gui.DrawContext;

public class VStackElement extends StackElement<VStackElement> {
    @Override
    public int getWidth() {
        int width = 0;
        for (Element element : elements) {
            if (element instanceof SpacerElement) continue;
            width = Math.max(width, element.getWidth());
        }
        return width;
    }

    @Override
    public int getHeight() {
        int height = 0;
        for (Element element : elements) {
            height += element.getHeight();
        }
        return height;
    }

    @Override
    public void draw(DrawContext context, int x, int y) {
        super.draw(context, x, y);

        int offsetY = y;
        for (Element element : elements) {
            element.draw(context, x, offsetY);
            offsetY += element.getHeight();
        }
    }
}
