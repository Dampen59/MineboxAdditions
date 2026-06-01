package io.dampen59.mineboxadditions.features.hud.elements.stack;

import io.dampen59.mineboxadditions.features.hud.elements.Element;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HStackElement extends StackElement<HStackElement> {
    @Override
    public int getWidth() {
        int width = 0;
        for (Element element : elements) {
            width += element.width();
        }
        return width;
    }

    @Override
    public int getHeight() {
        int height = 0;
        for (Element element : elements) {
            if (element instanceof SpacerElement) continue;
            height = Math.max(height, element.getHeight());
        }
        return height;
    }

    @Override
    public void draw(GuiGraphicsExtractor context, int x, int y) {
        super.draw(context, x, y);

        int offsetX = x;
        for (Element element : elements) {
            element.draw(context, offsetX, y);
            offsetX += element.width();
        }
    }
}
