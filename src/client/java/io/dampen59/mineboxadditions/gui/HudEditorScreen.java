package io.dampen59.mineboxadditions.gui;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.state.HUDState;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Map;

public class HudEditorScreen extends Screen {
    private Hud.Type dragging = null;
    private int offsetX, offsetY;

    public HudEditorScreen() {
        super(Text.of("HUD Editor"));
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            HUDState hudState = MineboxAdditionsClient.INSTANCE.modState.getHUDState();
            for (Map.Entry<Hud.Type, Hud> entry : hudState.getHuds().entrySet()) {
                Hud hud = entry.getValue();

                int screenCenterX = this.width / 2;
                if (entry.getKey() == Hud.Type.ITEM_PICKUP && mouseX > screenCenterX) {
                    mouseX += hud.getWidth();
                }

                if (isInBounds(mouseX, mouseY, hud.getX.get(), hud.getY.get(), hud.getWidth(), hud.getHeight())) {
                    dragging = entry.getKey();
                    offsetX = (int) mouseX - hud.getX.get();
                    offsetY = (int) mouseY - hud.getY.get();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            HUDState hudState = MineboxAdditionsClient.INSTANCE.modState.getHUDState();
            Hud hud = hudState.getHud(dragging);

            int screenCenterX = this.width / 2;
            int newHudX = (int) mouseX - offsetX;
            if (dragging == Hud.Type.ITEM_PICKUP && newHudX > screenCenterX) {
                hud.setX.accept(newHudX + hud.getWidth());
            } else {
                hud.setX.accept((int) mouseX - offsetX);
            }
            hud.setY.accept((int) mouseY - offsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != null) {
            dragging = null;
            AutoConfig.getConfigHolder(ModConfig.class).save();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        HUDState hudState = MineboxAdditionsClient.INSTANCE.modState.getHUDState();
        for (Hud hud : hudState.getHuds().values()) {
            hud.draw(context);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
