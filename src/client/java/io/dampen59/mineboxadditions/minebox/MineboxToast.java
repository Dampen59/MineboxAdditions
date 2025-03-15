package io.dampen59.mineboxadditions.minebox;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MineboxToast implements Toast {
    private final Identifier iconTexture;
    private final List<OrderedText> text;
    private Visibility visibility = Visibility.SHOW;

    public MineboxToast(TextRenderer textRenderer, Identifier iconTexture, Text title, Text description) {
        this.iconTexture = iconTexture;
        this.text = new ArrayList<>(2);
        this.text.addAll(textRenderer.wrapLines(title.copy().withColor(Color.YELLOW.getRGB()), 126));
        if (description != null) {
            this.text.addAll(textRenderer.wrapLines(description, 126));
        }
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.visibility = time > (5 * 1000L) ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int getHeight() {
        return 7 + this.getTextHeight() + 3;
    }

    private int getTextHeight() {
        return Math.max(this.text.size(), 2) * 11;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        int width = this.getWidth();
        int height = this.getHeight();

        context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("toast/advancement"), 0, 0, width, height);
        context.drawTexture(RenderLayer::getGuiTextured, iconTexture, 6, 6, 0, 0, 20, 20, 20, 20, 20, 20);

        int textY = (height - (this.text.size() * 11)) / 2;
        for (int i = 0; i < this.text.size(); i++) {
            OrderedText line = this.text.get(i);
            context.drawText(textRenderer, line, 30, textY + i * 11, 0xFFFFFF, false);
        }
    }
}