package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextElement extends Element {
    private Component text;
    private List<FormattedCharSequence> lines;
    private int maxWidth = -1;

    public TextElement(Component text) {
        setText(text);
    }

    public TextElement(Component text, int maxWidth) {
        this.maxWidth = maxWidth;
        setText(text);
    }

    private void updateText() {
        Font renderer = Minecraft.getInstance().font;
        if (maxWidth > 0) {
            lines = renderer.split(text, maxWidth);
        } else {
            lines = List.of(net.minecraft.locale.Language.getInstance().getVisualOrder(text));
        }
    }

    public void setText(Component text) {
        this.text = text;
        updateText();
    }

    public void setValue(Component text) {
        setText(text);
    }

    public void setLines(List<Component> components) {
        lines = components.stream()
                .map(c -> net.minecraft.locale.Language.getInstance().getVisualOrder(c))
                .collect(java.util.stream.Collectors.toList());
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        updateText();
    }

    @Override
    public int getWidth() {
        Font renderer = Minecraft.getInstance().font;
        int max = 0;
        for (FormattedCharSequence line : lines) {
            max = Math.max(max, renderer.width(line));
        }
        return max;
    }

    @Override
    public int getHeight() {
        Font renderer = Minecraft.getInstance().font;
        return renderer.lineHeight * lines.size();
    }

    @Override
    public void draw(GuiGraphicsExtractor context, int x, int y) {
        Font renderer = Minecraft.getInstance().font;
        int lineY = y;
        for (FormattedCharSequence line : lines) {
            context.text(renderer, line, x, lineY, 0xFFFFFFFF, true);
            lineY += renderer.lineHeight;
        }
    }
}
