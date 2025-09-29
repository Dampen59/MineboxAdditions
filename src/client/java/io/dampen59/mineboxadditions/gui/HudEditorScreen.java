package io.dampen59.mineboxadditions.gui;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.state.HUDState;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Map;

public class HudEditorScreen extends Screen {
    private Hud.Type dragging = null;
    private int offsetX, offsetY;
    private static final int PADDING = 2;

    public HudEditorScreen() {
        super(Text.of("HUD Editor"));
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            HUDState hudState = MineboxAdditions.INSTANCE.modState.getHUDState();
            for (Map.Entry<Hud.Type, Hud> entry : hudState.getHuds().entrySet()) {
                Hud hud = entry.getValue();
                int hudX = hud.getX();
                int hudY = hud.getY();

                if (isInBounds(mouseX, mouseY, hudX, hudY, hud.getWidth(), hud.getHeight())) {
                    dragging = entry.getKey();
                    offsetX = (int) mouseX - hudX;
                    offsetY = (int) mouseY - hudY;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            HUDState hudState = MineboxAdditions.INSTANCE.modState.getHUDState();
            Hud hud = hudState.getHud(dragging);

            int nextX = (int) mouseX - offsetX;
            int nextY = (int) mouseY - offsetY;

            nextX = Math.max(4, nextX);
            nextX = Math.min(this.width - hud.getWidth() - 4, nextX);
            nextY = Math.max(4, nextY);
            nextY = Math.min(this.height - hud.getHeight() - 4, nextY);

            boolean collisionOccurred;
            int iterations = 0;
            do {
                collisionOccurred = false;
                Rectangle hudBounds = new Rectangle(nextX, nextY, hud.getWidth(), hud.getHeight());

                for (Map.Entry<Hud.Type, Hud> entry : hudState.getHuds().entrySet()) {
                    if (entry.getKey() == dragging) continue;
                    Hud otherHud = entry.getValue();
                    Rectangle otherBounds = new Rectangle(
                            otherHud.getX() - PADDING,
                            otherHud.getY() - PADDING,
                            otherHud.getWidth() + PADDING * 2,
                            otherHud.getHeight() + PADDING * 2
                    );

                    if (hudBounds.intersects(otherBounds)) {
                        collisionOccurred = true;
                        double overlapX = (hudBounds.width / 2.0 + otherBounds.width / 2.0) - Math.abs(hudBounds.getCenterX() - otherBounds.getCenterX());
                        double overlapY = (hudBounds.height / 2.0 + otherBounds.height / 2.0) - Math.abs(hudBounds.getCenterY() - otherBounds.getCenterY());

                        if (overlapX < overlapY) {
                            if (hudBounds.getCenterX() < otherBounds.getCenterX()) {
                                nextX -= overlapX;
                            } else {
                                nextX += overlapX;
                            }
                        } else {
                            if (hudBounds.getCenterY() < otherBounds.getCenterY()) {
                                nextY -= overlapY;
                            } else {
                                nextY += overlapY;
                            }
                        }
                        hudBounds.setLocation(nextX, nextY);
                    }
                }

                iterations++;
            } while (collisionOccurred && iterations < 10);

            int screenCenterX = this.width / 2;
            if (dragging == Hud.Type.ITEM_PICKUP && nextX > screenCenterX) {
                hud.setX(nextX + hud.getWidth());
            } else {
                hud.setX(nextX);
            }
            hud.setY(nextY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != null) {
            dragging = null;
            AutoConfig.getConfigHolder(MineboxAdditionConfig.class).save();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        HUDState hudState = MineboxAdditions.INSTANCE.modState.getHUDState();
        for (Hud hud : hudState.getHuds().values()) {
            hud.draw(context);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
