package io.dampen59.mineboxadditions.gui;

import io.dampen59.mineboxadditions.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HudEditorScreen extends Screen {
    private final ModConfig config;
    private DragTarget dragging = null;
    private int offsetX, offsetY;

    private static final int PICKUPS_H = 20;
    private static final int PICKUPS_W = 180;


    enum DragTarget {
        RAIN, STORM, SHOP, FULL_MOON, HS_FILL_RATE, HS_FULL_IN, MERMAID_OFFER, ITEM_PICKUPS
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
            } else if (isInBounds(mouseX, mouseY, config.haverSackFillRateX, config.haverSackFillRateY)) {
                dragging = DragTarget.HS_FILL_RATE;
                offsetX = (int) mouseX - config.haverSackFillRateX;
                offsetY = (int) mouseY - config.haverSackFillRateY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.haversackFullInX, config.haversackFullInY)) {
                dragging = DragTarget.HS_FULL_IN;
                offsetX = (int) mouseX - config.haversackFullInX;
                offsetY = (int) mouseY - config.haversackFullInY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.mermaidRequestHudX, config.getMermaidRequestHudY)) {
                dragging = DragTarget.MERMAID_OFFER;
                offsetX = (int) mouseX - config.mermaidRequestHudX;
                offsetY = (int) mouseY - config.getMermaidRequestHudY;
                return true;
            } else if (isInBounds(mouseX, mouseY, config.itemPickupHudX, config.itemPickupHudY, PICKUPS_W, PICKUPS_H)) {
                dragging = DragTarget.ITEM_PICKUPS;
                offsetX = (int) mouseX - config.itemPickupHudX;
                offsetY = (int) mouseY - config.itemPickupHudY;
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
            } else if (dragging == DragTarget.HS_FILL_RATE) {
                config.haverSackFillRateX = (int) mouseX - offsetX;
                config.haverSackFillRateY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.HS_FULL_IN) {
                config.haversackFullInX = (int) mouseX - offsetX;
                config.haversackFullInY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.MERMAID_OFFER) {
                config.mermaidRequestHudX = (int) mouseX - offsetX;
                config.getMermaidRequestHudY = (int) mouseY - offsetY;
            } else if (dragging == DragTarget.ITEM_PICKUPS) {
                config.itemPickupHudX = (int) mouseX - offsetX;
                config.itemPickupHudY = (int) mouseY - offsetY;
            }

        return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Weather HUD
        drawLabel(context, config.rainHudX, config.rainHudY, "Next Rain: 00:00:00");
        drawLabel(context, config.stormHudX, config.stormHudY, "Next Storm: 00:00:00");

        // Shops HUD
        drawLabel(context, config.shopHudX, config.shopHudY, "Shop name: Shop offer");

        // Moon Phase HUD
        Identifier texture = Identifier.of("mineboxadditions", "textures/gui/moon_phases/full_moon.png");
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, config.fullMoonHudX, config.fullMoonHudY, 0, 0, 24, 24, 24, 24);

        // Haversack HUD
        drawLabel(context, config.haverSackFillRateX, config.haverSackFillRateY, "Haversack Fill Rate: 0.0/s");
        drawLabel(context, config.haversackFullInX, config.haversackFullInY, "Haversack Full in: 00:00:00");

        // Mermaid
        drawLabel(context, config.mermaidRequestHudX, config.getMermaidRequestHudY, "Mermaid request: 1x Bedrock");

        // Item Pickups
        drawPickupsPreview(context, config.itemPickupHudX, config.itemPickupHudY);

    }

    private void drawLabel(DrawContext context, int x, int y, String text) {
        context.fill(x - 2, y - 2, x + 130, y + 12, 0x55000000);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), x, y, 0xFFFFFFFF, false);
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 130 && mouseY >= y && mouseY <= y + 12;
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void drawPickupsPreview(DrawContext ctx, int x, int y) {
        ctx.fill(x - 2, y - 2, x + PICKUPS_W + 2, y + PICKUPS_H + 2, 0x55000000);

        ctx.fill(x, y + 2, x + 16, y + 18, 0x77FFFFFF);

        int rowY = y + 4;
        ctx.drawText(
                MinecraftClient.getInstance().textRenderer,
                Text.literal("+ 1 ").append(Text.literal("Bedrock (Item Pickups)")),
                x + 20,
                rowY,
                0xFFFFFFFF,
                true
        );
    }


    @Override
    public boolean shouldPause() {
        return false;
    }
}
