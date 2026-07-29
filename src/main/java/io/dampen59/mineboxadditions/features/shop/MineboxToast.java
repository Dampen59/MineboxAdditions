package io.dampen59.mineboxadditions.features.shop;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MineboxToast implements Toast {
    private static final int WIDTH = 160;
    private final Identifier iconTexture;
    private final List<FormattedCharSequence> text;
    private long startTime = -1;

    public MineboxToast(Font textRenderer, Identifier iconTexture, Component title, Component description) {
        this.iconTexture = iconTexture;
        this.text = new ArrayList<>(2);
        this.text.addAll(textRenderer.split(title.copy().withColor(Color.YELLOW.getRGB()), 126));
        if (description != null) {
            this.text.addAll(textRenderer.split(description, 126));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font textRenderer, long time) {
        if (startTime < 0) startTime = time;
        int height = 7 + Math.max(this.text.size(), 2) * 11 + 3;

        context.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("toast/advancement"), 0, 0, WIDTH, height);
        context.blit(RenderPipelines.GUI_TEXTURED, iconTexture, 6, 6, 0, 0, 20, 20, 20, 20, 20, 20);

        int textY = (height - (this.text.size() * 11)) / 2;
        for (int i = 0; i < this.text.size(); i++) {
            context.text(textRenderer, this.text.get(i), 30, textY + i * 11, 0xFFFFFFFF, false);
        }
    }

    @Override
    public Visibility getWantedVisibility() {
        return (startTime >= 0 && (System.currentTimeMillis() - startTime) > 5000L) ? Visibility.HIDE : Visibility.SHOW;
    }

    public void update(ToastManager manager, long time) {
        if (startTime < 0) startTime = time;
    }
}
