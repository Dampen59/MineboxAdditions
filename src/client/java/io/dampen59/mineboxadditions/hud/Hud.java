package io.dampen59.mineboxadditions.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Hud {
    private final Supplier<Integer> onGetX;
    private final Supplier<Integer> onGetY;
    private final Consumer<Integer> onSetX;
    private final Consumer<Integer> onSetY;
    private String icon;
    private Text text;

    public enum Type {
        RAIN, STORM, SHOP, FULL_MOON, HAVERSACK_RATE, HAVERSACK_FULL, MERMAID_OFFER, ITEM_PICKUP
    }

    public Hud(Supplier<Integer> getX, Supplier<Integer> getY,
        Consumer<Integer> setX, Consumer<Integer> setY,
        String icon, Text text) {
        this.onGetX = getX;
        this.onGetY = getY;
        this.onSetX = setX;
        this.onSetY = setY;
        this.icon = icon;
        this.text = text;
    }

    public Hud(Supplier<Integer> getX, Supplier<Integer> getY,
               Consumer<Integer> setX, Consumer<Integer> setY,
               String icon) {
        this(getX, getY, setX, setY, icon, null);
    }

    public int getX() {
        return this.onGetX.get();
    }

    public void setX(int x) {
        this.onSetX.accept(x);
    }

    public int getY() {
        return this.onGetY.get();
    }

    public void setY(int y) {
        this.onSetY.accept(y);
    }

    public int getWidth() {
        int width = 18;

        if (this.text != null) {
            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            width += (renderer.getWidth(this.text) + 4);
        }

        return width;
    }

    public int getHeight() {
        return 14;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public void drawPlate(DrawContext context, int x, int y, int w, int h) {
        context.fill(x + 1, y, x + w - 1, y + 1, 0x40000000);
        context.fill(x, y + 1, x + w, y + h - 1, 0x40000000);
        context.fill(x + 1, y + h - 1, x + w - 1, y + h, 0x40000000);
    }

    public void draw(DrawContext context) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        int x = getX();
        int y = getY();
        this.drawPlate(context, x, y, getWidth(), getHeight());

        Identifier icon = Identifier.of("mineboxadditions", "textures/icons/" + this.icon + ".png");

        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x + 4, y + 2, 0, 0, 10, 10, 10, 10);
        if (this.text != null) {
            context.drawText(renderer, text, x + 18, y + 3, 0xFFFFFFFF, true);
        }
    }
}