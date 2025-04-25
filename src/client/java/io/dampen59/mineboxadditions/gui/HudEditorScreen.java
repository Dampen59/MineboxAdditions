package io.dampen59.mineboxadditions.gui;

import io.dampen59.mineboxadditions.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HudEditorScreen extends Screen {
    private final ModConfig config;
    private DragTarget dragging = null;
    private int offsetX, offsetY;

    enum DragTarget {
        RAIN, STORM, SHOP, FULL_MOON
    }

    public HudEditorScreen(ModConfig config) {
        super(Text.literal("HUD Editor"));
        this.config = config;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isInBounds(mouseX, mouseY, config.rainHudX, config.rainHudY)) {
                dragging = DragTarget.RAIN;
                offsetX = (int) mouseX - config.rainHudX;
                offsetY = (int) mouseY - config.rainHudY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.stormHudX, config.stormHudY)) {
                dragging = DragTarget.STORM;
                offsetX = (int) mouseX - config.stormHudX;
                offsetY = (int) mouseY - config.stormHudY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.shopHudX, config.shopHudY)) {
                dragging = DragTarget.SHOP;
                offsetX = (int) mouseX - config.shopHudX;
                offsetY = (int) mouseY - config.shopHudY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.fullMoonHudX, config.fullMoonHudY)) {
                dragging = DragTarget.FULL_MOON;
                offsetX = (int) mouseX - config.fullMoonHudX;
                offsetY = (int) mouseY - config.fullMoonHudY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        AutoConfig.getConfigHolder(ModConfig.class).save();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            if (dragging == DragTarget.RAIN) {
                config.rainHudX = (int) mouseX - offsetX;
                config.rainHudY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.STORM) {
                config.stormHudX = (int) mouseX - offsetX;
                config.stormHudY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.SHOP) {
                config.shopHudX = (int) mouseX - offsetX;
                config.shopHudY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.FULL_MOON) {
                config.fullMoonHudX = (int) mouseX - offsetX;
                config.fullMoonHudY = (int) mouseY - offsetY;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        drawLabel(context, config.rainHudX, config.rainHudY, "Next Rain: 00:00:00");
        drawLabel(context, config.stormHudX, config.stormHudY, "Next Storm: 00:00:00");
        drawLabel(context, config.shopHudX, config.shopHudY, "Shop name: Shop offer");

        Identifier texture = Identifier.of("mineboxadditions", "textures/gui/moon_phases/full_moon.png");
        context.drawTexture(RenderLayer::getGuiTextured, texture, config.fullMoonHudX, config.fullMoonHudY, 0, 0, 24, 24, 24, 24);
    }

    private void drawLabel(DrawContext context, int x, int y, String text) {
        context.fill(x - 2, y - 2, x + 130, y + 12, 0x55000000);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), x, y, 0xFFFFFF, false);
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 130 && mouseY >= y && mouseY <= y + 12;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
