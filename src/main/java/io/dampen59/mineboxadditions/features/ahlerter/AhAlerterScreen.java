package io.dampen59.mineboxadditions.features.ahlerter;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AhAlerterScreen extends Screen {
    private static final int PAD         = 8;
    private static final int HEADER_H    = 42;
    private static final int GRID_COLS   = 4;
    private static final int CELL_H      = 88;
    private static final int CELL_GAP    = 6;
    private static final int ICON_SZ     = 48;
    private static final int ALERT_ROW_H = 46;
    private static final int ALERT_PAD   = 8;
    private static final int DEL_W       = 18;

    private int divX;

    private List<MineboxItem> allItems      = List.of();
    private List<MineboxItem> filteredItems = List.of();
    private EditBox searchField;
    private int gridScroll  = 0;
    private int alertScroll = 0;
    private int cols        = GRID_COLS;
    private int cellW       = 72;

    private final Map<String, Identifier> texCache  = new HashMap<>();
    private final Set<String>             texFailed = new HashSet<>();

    public AhAlerterScreen() {
        super(Component.literal("AH-Lerter!"));
    }

    @Override
    protected void init() {
        divX = this.width * 3 / 5;

        List<MineboxItem> items = MineboxAdditions.INSTANCE.state.getMbxItems();
        allItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        Set<String> catalog = MineboxAdditions.INSTANCE.state.getAuctionCatalogIds();
        if (!catalog.isEmpty()) {
            allItems.removeIf(item -> !catalog.contains(item.getId()));
        }
        filteredItems = new ArrayList<>(allItems);

        int searchW = Math.min(180, divX / 3);
        searchField = new EditBox(font, divX - searchW - PAD, (HEADER_H - 16) / 2, searchW, 16, Component.empty());
        searchField.setHint(Component.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setResponder(this::updateFilter);
        addRenderableWidget(searchField);

        cols  = GRID_COLS;
        cellW = Math.max(48, (divX - 2 * PAD - (cols - 1) * CELL_GAP) / cols);
        gridScroll  = 0;
        alertScroll = 0;
    }

    private void updateFilter(String query) {
        String q = query.toLowerCase().trim();
        filteredItems = q.isEmpty()
                ? new ArrayList<>(allItems)
                : allItems.stream().filter(item -> matchesSearch(item, q)).toList();
        gridScroll = 0;
    }

    private boolean matchesSearch(MineboxItem item, String q) {
        if (item.getId().toLowerCase().contains(q)) return true;
        return MineboxItem.getDisplayName(item).getString().toLowerCase().contains(q);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0,        0, divX,        this.height, 0xCC0D0D0D);
        ctx.fill(divX + 1, 0, this.width,  this.height, 0xCC0A0A0A);
        ctx.fill(0, 0, this.width, HEADER_H, 0xCC141414);
        ctx.fill(0, HEADER_H, this.width, HEADER_H + 1, 0x44FFAA00);
        ctx.fill(divX, 0, divX + 1, this.height, 0x33FFFFFF);

        ctx.text(font,
                Component.literal("AH-Lerter!").withStyle(s -> s.withBold(true).withColor(0xFFFFAA00)),
                PAD, 8, 0xFFFFAA00, true);
        int itemCount = filteredItems.size();
        String subText = itemCount + " item" + (itemCount == 1 ? "" : "s");
        ctx.text(font, Component.literal(subText), PAD, 8 + font.lineHeight + 4, 0xFF555555, false);

        ctx.fill(searchField.getX() - 2, searchField.getY() - 2,
                searchField.getX() + searchField.getWidth() + 2,
                searchField.getY() + searchField.getHeight() + 2, 0x44FFFFFF);

        int alertCount = Config.ahAlerts.alerts.size();
        String alertHeader = "Active Alerts" + (alertCount > 0 ? " (" + alertCount + ")" : "");
        ctx.text(font,
                Component.literal(alertHeader).withStyle(s -> s.withBold(true)),
                divX + ALERT_PAD, 8, 0xFFFFFFFF, true);
        ctx.text(font, Component.literal("Click an alert to edit it"),
                divX + ALERT_PAD, 8 + font.lineHeight + 4, 0xFF444444, false);

        drawItemGrid(ctx, mouseX, mouseY);
        drawAlertsPanel(ctx, mouseX, mouseY);

        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    // ── Left panel ────────────────────────────────────────────────────────────

    private void drawItemGrid(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int gridTop    = HEADER_H + PAD;
        int gridBottom = this.height - PAD;
        int gridH      = gridBottom - gridTop;
        int rows       = (filteredItems.size() + cols - 1) / cols;
        int totalH     = rows * (CELL_H + CELL_GAP);
        int maxScroll  = Math.max(0, totalH - gridH);
        gridScroll = Math.max(0, Math.min(gridScroll, maxScroll));

        ctx.enableScissor(0, gridTop, divX, gridBottom);

        for (int i = 0; i < filteredItems.size(); i++) {
            MineboxItem item = filteredItems.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx  = PAD + col * (cellW + CELL_GAP);
            int cy  = gridTop + row * (CELL_H + CELL_GAP) - gridScroll;
            if (cy + CELL_H < gridTop || cy >= gridBottom) continue;
            boolean hovered = mouseX >= cx && mouseX < cx + cellW
                    && mouseY >= Math.max(cy, gridTop) && mouseY < Math.min(cy + CELL_H, gridBottom)
                    && mouseX < divX;
            drawCell(ctx, item, cx, cy, hovered, gridTop);
        }

        ctx.disableScissor();

        if (filteredItems.isEmpty()) {
            String msg = allItems.isEmpty() ? "No item data loaded" : "No items found";
            ctx.text(font, Component.literal(msg),
                    (divX - font.width(msg)) / 2, HEADER_H + 20, 0xFF444444, false);
        }
    }

    private void drawCell(GuiGraphicsExtractor ctx, MineboxItem item, int cx, int cy, boolean hovered, int gridTop) {
        if (Math.max(cy, gridTop) >= cy + CELL_H) return;

        int bg = hovered ? 0x33FFFFFF : 0x1A000000;
        ctx.fill(cx, cy, cx + cellW, cy + CELL_H, bg);

        int rarityColor = 0xFF666666;
        if (item.getRarity() != null) {
            rarityColor = RaritiesUtils.getRarityColor(item.getRarity().toLowerCase()).getRGB() | 0xFF000000;
        }

        ctx.fill(cx,              cy,              cx + cellW,      cy + 2,          rarityColor);
        ctx.fill(cx,              cy + CELL_H - 2, cx + cellW,      cy + CELL_H,     rarityColor);
        ctx.fill(cx,              cy,              cx + 2,            cy + CELL_H,     rarityColor);
        ctx.fill(cx + cellW - 2, cy,              cx + cellW,      cy + CELL_H,     rarityColor);

        int iconX = cx + (cellW - ICON_SZ) / 2;
        int iconY = cy + 4;
        Identifier tex = getItemTex(item);
        if (tex != null) {
            float scale = ICON_SZ / 16.0f;
            Matrix3x2f backup = new Matrix3x2f(ctx.pose());
            ctx.pose().translate(iconX, iconY);
            ctx.pose().scale(scale, scale);
            ctx.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0, 0, 16, 16, 16, 16);
            ctx.pose().set(backup);
        }

        if (!Config.ahAlerts.getAlertsForItem(item.getId()).isEmpty()) {
            ctx.fill(cx + cellW - 14, cy + 2, cx + cellW - 2, cy + 14, 0xFFFF4400);
            ctx.text(font, Component.literal("!").withStyle(s -> s.withBold(true)),
                    cx + cellW - 10, cy + 3, 0xFFFFFFFF, false);
        }

        String name   = MineboxItem.getDisplayName(item).getString();
        int    maxW   = cellW - 6;
        if (font.width(name) > maxW) {
            while (font.width(name + "…") > maxW && name.length() > 1)
                name = name.substring(0, name.length() - 1);
            name += "…";
        }
        final int fc = rarityColor;
        ctx.text(font, Component.literal(name).withStyle(s -> s.withColor(fc)),
                cx + (cellW - font.width(name)) / 2,
                cy + CELL_H - font.lineHeight - 6,
                rarityColor, false);
    }

    private void drawAlertsPanel(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int rx         = divX + 1;
        int rw         = this.width - rx;
        int bodyTop    = HEADER_H + PAD;
        int bodyBottom = this.height - PAD;
        int bodyH      = bodyBottom - bodyTop;

        List<AhAlert> alerts = Config.ahAlerts.alerts;
        int totalH    = alerts.size() * (ALERT_ROW_H + 2);
        int maxScroll = Math.max(0, totalH - bodyH);
        alertScroll   = Math.max(0, Math.min(alertScroll, maxScroll));

        if (alerts.isEmpty()) {
            String msg = "No alerts yet";
            ctx.text(font, Component.literal(msg),
                    rx + (rw - font.width(msg)) / 2, bodyTop + 20, 0xFF333333, false);
            return;
        }

        ctx.enableScissor(rx, bodyTop, this.width, bodyBottom);

        for (int i = 0; i < alerts.size(); i++) {
            AhAlert alert = alerts.get(i);
            int ry = bodyTop + i * (ALERT_ROW_H + 2) - alertScroll;
            if (ry + ALERT_ROW_H < bodyTop || ry >= bodyBottom) continue;
            drawAlertRow(ctx, alert, rx, ry, rw, mouseX, mouseY, bodyTop);
        }

        ctx.disableScissor();
    }

    private void drawAlertRow(GuiGraphicsExtractor ctx, AhAlert alert,
                               int rx, int ry, int rw,
                               int mouseX, int mouseY, int bodyTop) {
        boolean rowHovered = mouseX >= rx && mouseX < rx + rw - DEL_W - 2
                && mouseY >= Math.max(ry, bodyTop) && mouseY < ry + ALERT_ROW_H;
        boolean delHovered = mouseX >= rx + rw - DEL_W - 2 && mouseX < rx + rw
                && mouseY >= Math.max(ry, bodyTop) && mouseY < ry + ALERT_ROW_H;

        ctx.fill(rx, ry, rx + rw, ry + ALERT_ROW_H, rowHovered ? 0x22FFFFFF : 0x11FFFFFF);
        ctx.fill(rx, ry + ALERT_ROW_H, rx + rw, ry + ALERT_ROW_H + 2, 0x22000000);

        MineboxItem mbxItem = MineboxAdditions.INSTANCE.state.getItemById(alert.itemId);
        int rarityColor = 0xFF555555;
        if (mbxItem != null && mbxItem.getRarity() != null) {
            rarityColor = RaritiesUtils.getRarityColor(mbxItem.getRarity().toLowerCase()).getRGB() | 0xFF000000;
        }
        ctx.fill(rx, ry, rx + 2, ry + ALERT_ROW_H, rarityColor);

        int iconX = rx + ALERT_PAD;
        int iconY = ry + (ALERT_ROW_H - 16) / 2;
        Identifier tex = mbxItem != null ? getItemTex(mbxItem) : null;
        if (tex != null) {
            ctx.blit(RenderPipelines.GUI_TEXTURED, tex, iconX, iconY, 0, 0, 16, 16, 16, 16);
        }

        int textX = iconX + 18;
        int maxNameW = rw - ALERT_PAD - 18 - DEL_W - 6;
        String name = mbxItem != null ? MineboxItem.getDisplayName(mbxItem).getString() : alert.itemId;
        if (font.width(name) > maxNameW) {
            while (font.width(name + "…") > maxNameW && name.length() > 1)
                name = name.substring(0, name.length() - 1);
            name += "…";
        }
        final int fc = rarityColor;
        ctx.text(font, Component.literal(name).withStyle(s -> s.withColor(fc)),
                textX, ry + 6, rarityColor, false);

        String cond = buildConditionSummary(alert);
        if (font.width(cond) > maxNameW) {
            while (font.width(cond + "…") > maxNameW && cond.length() > 1)
                cond = cond.substring(0, cond.length() - 1);
            cond += "…";
        }
        ctx.text(font, Component.literal(cond), textX, ry + 6 + font.lineHeight + 3, 0xFF777777, false);

        int delX = rx + rw - DEL_W - 2;
        ctx.fill(delX, ry + (ALERT_ROW_H - 16) / 2, delX + DEL_W, ry + (ALERT_ROW_H + 16) / 2,
                delHovered ? 0xAACC2200 : 0x44882200);
        ctx.text(font, Component.literal("×"),
                delX + (DEL_W - font.width("×")) / 2,
                ry + (ALERT_ROW_H - font.lineHeight) / 2,
                delHovered ? 0xFFFFFFFF : 0xFFAA4422, false);
    }

    private String buildConditionSummary(AhAlert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append(alert.maxPrice != null ? "≤ " + formatCoins(alert.maxPrice) : "Any price");
        if (alert.statFilters != null && !alert.statFilters.isEmpty()) {
            for (Map.Entry<String, AhAlert.StatFilter> e : alert.statFilters.entrySet()) {
                String shortKey = e.getKey().replaceFirst("^mbx_stats_", "").toUpperCase();
                AhAlert.StatFilter f = e.getValue();
                sb.append("  ").append(shortKey).append(": ");
                if (f.min != null && f.max != null) sb.append(f.min).append("–").append(f.max);
                else if (f.min != null) sb.append("≥").append(f.min);
                else if (f.max != null) sb.append("≤").append(f.max);
            }
        }
        return sb.toString();
    }

    private String formatCoins(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000) return String.format("%.0fk", n / 1_000.0);
        return String.valueOf(n);
    }

    private Identifier getItemTex(MineboxItem item) {
        String id = item.getId();
        if (texFailed.contains(id)) return null;
        Identifier cached = texCache.get(id);
        if (cached != null) return cached;
        Identifier fromList = ItemListWidget.ItemEntry.getTexture(id);
        if (fromList != null) { texCache.put(id, fromList); return fromList; }
        if (item.getTexture() != null) {
            Identifier loaded = ItemListWidget.ItemEntry.loadTexture(id, item.getTexture());
            if (loaded != null) { texCache.put(id, loaded); return loaded; }
        }
        texFailed.add(id);
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (mx < divX) {
            int gridTop = HEADER_H + PAD;
            for (int i = 0; i < filteredItems.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int cx  = PAD + col * (cellW + CELL_GAP);
                int cy  = gridTop + row * (CELL_H + CELL_GAP) - gridScroll;
                if (mx >= cx && mx < cx + cellW
                        && my >= Math.max(cy, gridTop) && my < cy + CELL_H) {
                    this.minecraft.gui.setScreen(new AhAlertEditScreen(filteredItems.get(i), this));
                    return true;
                }
            }
            return super.mouseClicked(event, doubleClick);
        }

        int rx      = divX + 1;
        int rw      = this.width - rx;
        int bodyTop = HEADER_H + PAD;
        List<AhAlert> alerts = Config.ahAlerts.alerts;
        for (int i = 0; i < alerts.size(); i++) {
            AhAlert alert = alerts.get(i);
            int ry = bodyTop + i * (ALERT_ROW_H + 2) - alertScroll;
            if (my < Math.max(ry, bodyTop) || my >= ry + ALERT_ROW_H) continue;

            int delX = rx + rw - DEL_W - 2;
            if (mx >= delX) {
                Config.ahAlerts.removeAlert(i);
                ConfigManager.save();
                return true;
            } else if (mx >= rx) {
                MineboxItem mbxItem = MineboxAdditions.INSTANCE.state.getItemById(alert.itemId);
                if (mbxItem != null) {
                    this.minecraft.gui.setScreen(new AhAlertEditScreen(mbxItem, this));
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        if (mouseY <= HEADER_H) return false;
        if (mouseX < divX) {
            gridScroll -= (int) (v * 20);
        } else {
            alertScroll -= (int) (v * 20);
        }
        return true;
    }

    public boolean shouldPause() {
        return false;
    }
}
