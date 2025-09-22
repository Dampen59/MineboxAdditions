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
            int screenCenterX = this.width / 2;
            for (Map.Entry<Hud.Type, Hud> entry : hudState.getHuds().entrySet()) {
                Hud.Type type = entry.getKey();
                Hud hud = entry.getValue();

                int boxX = hud.getX.get();
                if (type == Hud.Type.ITEM_PICKUP && boxX > screenCenterX) {
                    boxX -= hud.getWidth();
                }
                int boxY = hud.getY.get();

                if (isInBounds(mouseX, mouseY, boxX, boxY, hud.getWidth(), hud.getHeight())) {
                    dragging = type;
                    offsetX = (int) mouseX - boxX;
                    offsetY = (int) mouseY - boxY;
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
            int newBoxX = (int) mouseX - offsetX;
            int newBoxY = (int) mouseY - offsetY;

            if (dragging == Hud.Type.ITEM_PICKUP && hud.getX.get() > screenCenterX) {
                hud.setX.accept(newBoxX + hud.getWidth());
            } else {
                hud.setX.accept(newBoxX);
            }
            hud.setY.accept(newBoxY);
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
