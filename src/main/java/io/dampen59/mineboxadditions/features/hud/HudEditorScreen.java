package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;

public class HudEditorScreen extends Screen {
    private DragContext dragContext = null;
    private boolean dirty = false;
    private static final int PADDING = 2;
    private static final int MARGIN = 4;

    public HudEditorScreen() {
        super(Text.of("HUD Editor"));
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
            for (Map.Entry<Hud.Type, Hud> entry : HudManager.INSTANCE.getHuds().entrySet()) {
                if (entry.getKey() == dragContext.type) continue;
                Hud otherHud = entry.getValue();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Map.Entry<Hud.Type, Hud> entry : HudManager.INSTANCE.getHuds().entrySet()) {
            Hud hud = entry.getValue();
            int hudX = hud.getX();
            int hudY = hud.getY();

            if (isInBounds(mouseX, mouseY, hudX, hudY, hud.getWidth(), hud.getHeight())) {
                dragContext = new DragContext(entry.getKey(), button, (int) mouseX - hudX, (int) mouseY - hudY);
                if (button == 1) {
                    hud.setState(!hud.getState());
                    dirty = true;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragContext != null && dragContext.button == 0) {
            Hud hud = HudManager.INSTANCE.getHud(dragContext.type);
            Point clamped = clampToScreen(hud, (int)mouseX - dragContext.offsetX, (int)mouseY - dragContext.offsetY);
            Point resolved = resolveCollisions(hud, clamped.x, clamped.y);

            if (dragContext.type == Hud.Type.ITEM_PICKUP && resolved.x > this.width / 2) {
                hud.setX(resolved.x + hud.getWidth());
            } else {
                hud.setX(resolved.x);
            }
            hud.setY(resolved.y);
            dirty = true;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragContext != null) {
            Hud hud = HudManager.INSTANCE.getHud(dragContext.type);
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
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (Hud hud : HudManager.INSTANCE.getHuds().values()) {
            if (hud.getState()) hud.draw(context);
            else hud.drawDisabled(context);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class DragContext {
        final Hud.Type type;
        final int button;
        final int offsetX, offsetY;

        DragContext(Hud.Type type, int button, int offsetX, int offsetY) {
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
