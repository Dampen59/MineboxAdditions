package io.dampen59.mineboxadditions.features.hud.elements.stack;

import io.dampen59.mineboxadditions.features.hud.elements.Element;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class StackElement<T extends StackElement<T>> extends Element {
    protected final List<Element> elements = new ArrayList<>();
    private Integer bgColor;

    public T add(Element element) {
        elements.add(element);
        return (T)this;
    }

    public T add(Element... elements) {
        for (Element element : elements) {
            this.elements.add(element);
        }
        return (T)this;
    }

    public void remove(int index) {
        elements.remove(index);
    }

    public Integer getColor() {
        return bgColor;
    }

    public T setColor(Integer color) {
        this.bgColor = color;
        return (T)this;
    }

    @Override
    public void draw(DrawContext context, int x, int y) {
        if (getColor() != null) {
            int color = getColor();
            int w = getWidth();
            int h = getHeight();
            context.fill(x + 1, y, x + w - 1, y + 1, color);
            context.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
            context.fill(x, y + 1, x + w, y + h - 1, color);
        }
    }
}
