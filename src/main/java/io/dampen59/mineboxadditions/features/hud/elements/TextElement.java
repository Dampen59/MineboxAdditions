package io.dampen59.mineboxadditions.features.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class TextElement extends Element {
    private Text text;
    private List<OrderedText> lines;
    private int maxWidth = -1;

    public TextElement(Text text) {
        setText(text);
    }

    public TextElement(Text text, int maxWidth) {
        this.maxWidth = maxWidth;
        setText(text);
    }

    private void updateText() {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        if (maxWidth > 0) {
            lines = renderer.wrapLines(text, maxWidth);
        } else {
            lines = List.of(text.asOrderedText());
        }
    }

    public void setText(Text text) {
        this.text = text;
        updateText();
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        updateText();
    }

    @Override
    public int getWidth() {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        int max = 0;
        for (OrderedText line : lines) {
            max = Math.max(max, renderer.getWidth(line));
        }
        return max;
    }

    @Override
    public int getHeight() {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        return renderer.fontHeight * lines.size();
    }

    @Override
    public void draw(DrawContext context, int x, int y) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        int lineY = y;
        for (OrderedText line : lines) {
            context.drawText(renderer, line, x, lineY, 0xFFFFFFFF, true);
            lineY += renderer.fontHeight;
        }
    }
}
