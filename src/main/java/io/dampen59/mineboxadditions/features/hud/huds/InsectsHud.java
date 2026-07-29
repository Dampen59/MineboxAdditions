package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.features.item.Insect;

import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class InsectsHud extends Hud {

    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP  = 2;
    private static final int MAX_COLS  = 8;

    private List<String> activeInsects = List.of();

    public InsectsHud() {
        super(
            () -> HudsConfig.insects.enabled,
            s  -> HudsConfig.insects.enabled = s,
            () -> HudPositions.insects.x,
            x  -> HudPositions.insects.x = x,
            () -> HudPositions.insects.y,
            y  -> HudPositions.insects.y = y
        );
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft mc) {
        List<Insect> data = MineboxAdditions.INSTANCE.state.getInsects();
        if (mc.level == null || !Utils.isTimeKnown() || data == null) { activeInsects = List.of(); return; }

        int hour      = Utils.getTime().getHour();
        boolean rain  = mc.level.isRaining();
        int moonPhase = MineboxAdditions.INSTANCE.state.getCurrentMoonPhase();

        List<String> next = new ArrayList<>();
        for (Insect insect : data) {
            if (HudsConfig.insects.isInsectEnabled(insect.getId()) && insect.canSpawn(hour, rain, moonPhase)) {
                next.add(insect.getId());
            }
        }
        next.sort(String::compareTo);
        activeInsects = next;
    }

    @Override
    public void draw(GuiGraphicsExtractor ctx) {
        if (activeInsects.isEmpty()) return;
        renderGrid(ctx, activeInsects, 0x40000000);
    }

    @Override
    public void drawDisabled(GuiGraphicsExtractor ctx) {
        List<Insect> data = MineboxAdditions.INSTANCE.state.getInsects();
        if (data == null) return;
        List<String> preview = data.stream().map(Insect::getId).sorted().collect(Collectors.toList());
        renderGrid(ctx, preview.subList(0, Math.min(4, preview.size())), 0x40FF0000);
    }

    private void renderGrid(GuiGraphicsExtractor ctx, List<String> insects, int bgColor) {
        int n    = insects.size();
        int cols = Math.min(n, MAX_COLS);
        int rows = (n + cols - 1) / cols;
        int w    = cols * (ICON_SIZE + ICON_GAP) + ICON_GAP;
        int h    = rows * (ICON_SIZE + ICON_GAP) + ICON_GAP;

        int x = getX(), y = getY();
        ctx.fill(x + 1, y,         x + w - 1, y + 1,     bgColor);
        ctx.fill(x,     y + 1,     x + w,     y + h - 1, bgColor);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h,     bgColor);

        for (int i = 0; i < n; i++) {
            Identifier tex = resolveTexture(insects.get(i));
            if (tex == null) continue;
            int col = i % cols;
            int row = i / cols;
            int ix  = getX() + ICON_GAP + col * (ICON_SIZE + ICON_GAP);
            int iy  = getY() + ICON_GAP + row * (ICON_SIZE + ICON_GAP);
            ctx.blit(RenderPipelines.GUI_TEXTURED, tex, ix, iy, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }
    }

    private static Identifier resolveTexture(String id) {
        Identifier cached = ItemListWidget.ItemEntry.getTexture(id);
        if (cached != null) return cached;
        MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
        if (item != null) {
            return ItemListWidget.ItemEntry.getTextureCache()
                .computeIfAbsent(id, k -> ItemListWidget.ItemEntry.loadTexture(id, item.getTexture()));
        }
        return null;
    }

    @Override
    public int getWidth() {
        int n = Math.max(activeInsects.size(), 1);
        return Math.min(n, MAX_COLS) * (ICON_SIZE + ICON_GAP) + ICON_GAP;
    }

    @Override
    public int getHeight() {
        int n    = Math.max(activeInsects.size(), 1);
        int cols = Math.min(n, MAX_COLS);
        int rows = (n + cols - 1) / cols;
        return rows * (ICON_SIZE + ICON_GAP) + ICON_GAP;
    }

    @Override
    public StackElement init() {
        return new VStackElement().add(new SpacerElement(ICON_SIZE + ICON_GAP * 2));
    }
}
