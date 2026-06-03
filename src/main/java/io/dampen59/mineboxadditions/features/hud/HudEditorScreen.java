package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class HudEditorScreen extends Screen {
    private DragContext dragContext = null;
    private boolean dirty = false;
    private static final int PADDING = 2;
    private static final int MARGIN = 4;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private Point clampToScreen(Hud hud, int x, int y) {
        int clampedX = Math.max(MARGIN, Math.min(this.width - hud.getWidth() - MARGIN, x));
        int clampedY = Math.max(MARGIN, Math.min(this.height - hud.getHeight() - MARGIN, y));
        return new Point(clampedX, clampedY);
    }

    private Point resolveCollisions(Hud hud, int x, int y) {
        Bounds hudBounds = new Bounds(x, y, hud.getWidth(), hud.getHeight());

        int iterations = 0;
        boolean collision;
        do {
            collision = false;
            for (Hud otherHud : HudManager.INSTANCE.getAll()) {
                if (otherHud.getClass() == dragContext.type) continue;
                Bounds otherBounds = new Bounds(
                        otherHud.getX() - PADDING,
                        otherHud.getY() - PADDING,
                        otherHud.getWidth() + PADDING * 2,
                        otherHud.getHeight() + PADDING * 2
                );
                if (hudBounds.intersects(otherBounds)) {
                    collision = true;
                    double overlapX = (hudBounds.width / 2.0 + otherBounds.width / 2.0) - Math.abs(hudBounds.centerX() - otherBounds.centerX());
                    double overlapY = (hudBounds.height / 2.0 + otherBounds.height / 2.0) - Math.abs(hudBounds.centerY() - otherBounds.centerY());
                    if (overlapX < overlapY) x += (hudBounds.centerX() < otherBounds.centerX()) ? -overlapX : overlapX;
                    else y += (hudBounds.centerY() < otherBounds.centerY()) ? -overlapY : overlapY;
                    hudBounds.set(x, y);
                }
            }
            iterations++;
        } while (collision && iterations < 10);

        return new Point(x, y);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(); double mouseY = event.y(); int button = event.button();
        for (Hud hud : HudManager.INSTANCE.getAll()) {
            int hudX = hud.getX();
            int hudY = hud.getY();

            if (isInBounds(mouseX, mouseY, hudX, hudY, hud.getWidth(), hud.getHeight())) {
                dragContext = new DragContext(hud.getClass(), button, (int) mouseX - hudX, (int) mouseY - hudY);
                if (button == 1) {
                    hud.setState(!hud.getState());
                    dirty = true;
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x(); double mouseY = event.y();
        if (dragContext != null && dragContext.button == 0) {
            Hud hud = HudManager.INSTANCE.get(dragContext.type);
            Point clamped = clampToScreen(hud, (int)mouseX - dragContext.offsetX, (int)mouseY - dragContext.offsetY);
            Point resolved = resolveCollisions(hud, clamped.x, clamped.y);

            hud.setX(resolved.x);
            hud.setY(resolved.y);
            dirty = true;
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragContext != null) {
            Hud hud = HudManager.INSTANCE.get(dragContext.type);
            boolean outOfBounds = hud.getX() < 0 || hud.getX() + hud.getWidth() > this.width ||
                    hud.getY() < 0 || hud.getY() + hud.getHeight() > this.height;

            if (outOfBounds) {
                hud.setX((this.width - hud.getWidth()) / 2);
                hud.setY((this.height - hud.getHeight()) / 2);
                dirty = true;
            }
        }

        if (dirty) {
            ConfigManager.save();
            dirty = false;
        }
        dragContext = null;
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        for (Hud hud : HudManager.INSTANCE.getAll()) {
            if (hud.getState()) hud.draw(context);
            else hud.drawDisabled(context);
        }
    }

    public boolean shouldPause() {
        return false;
    }

    private static class DragContext {
        final Class<? extends Hud> type;
        final int button;
        final int offsetX, offsetY;

        DragContext(Class<? extends Hud> type, int button, int offsetX, int offsetY) {
            this.type = type;
            this.button = button;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    private static class Bounds {
        int x, y, width, height;

        Bounds(int x, int y, int width, int height) {
            set(x, y, width, height);
        }

        void set(int x, int y) { this.x = x; this.y = y; }
        void set(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }

        double centerX() { return x + width / 2.0; }
        double centerY() { return y + height / 2.0; }

        boolean intersects(Bounds other) {
            return this.x < other.x + other.width &&
                    this.x + this.width > other.x &&
                    this.y < other.y + other.height &&
                    this.y + this.height > other.y;
        }
    }
}
