package io.dampen59.mineboxadditions.features.ahlerter;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.features.item.MineboxStat;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AhAlertEditScreen extends Screen {
    private static final int PANEL_W = 340;
    private static final int PAD = 12;
    private static final int ICON_SZ = 48;
    private static final int INPUT_H = 16;
    private static final int ROW_H = 24;

    private final MineboxItem item;
    private final Screen parent;

    private EditBox priceField;
    private final Map<String, EditBox[]> statFields = new LinkedHashMap<>();

    private int panelX, panelY, panelH;
    private int priceRowY;
    private final Map<String, Integer> statRowYMap = new LinkedHashMap<>();
    private int buttonsY;
    private int alertsLabelY;
    private final List<Integer> alertRowYList = new ArrayList<>();

    public AhAlertEditScreen(MineboxItem item, Screen parent) {
        super(Component.literal("AH-Lerter!"));
        this.item = item;
        this.parent = parent;
    }

    private int computePanelH() {
        int h = PAD + ICON_SZ + PAD;
        h += 1 + PAD;
        h += ROW_H + PAD;
        int statCount = item.getMbxStats() != null ? item.getMbxStats().size() : 0;
        if (statCount > 0) {
            h += (ROW_H + 4) * statCount + PAD;
        }
        h += ROW_H + PAD;
        List<AhAlert> existing = Config.ahAlerts.getAlertsForItem(item.getId());
        if (!existing.isEmpty()) {
            h += 1 + 4 + font.lineHeight + 8;
            h += (ROW_H + 4) * existing.size();
        }
        h += PAD;
        return h;
    }

    @Override
    protected void init() {
        statFields.clear();
        statRowYMap.clear();
        alertRowYList.clear();

        panelH = computePanelH();
        panelX = (this.width - PANEL_W) / 2;
        panelY = Math.max(4, (this.height - panelH) / 2);

        int y = panelY + PAD + ICON_SZ + PAD + 1 + PAD;

        // price
        priceRowY = y;
        int priceLabelW = font.width("Max price ≤") + 4;
        int priceFieldW = PANEL_W - PAD * 2 - priceLabelW - 4;
        priceField = new EditBox(font, panelX + PAD + priceLabelW + 4, y + (ROW_H - INPUT_H) / 2,
                priceFieldW, INPUT_H, Component.empty());
        priceField.setHint(Component.literal("Any price"));
        priceField.setMaxLength(16);
        addRenderableWidget(priceField);
        y += ROW_H + PAD;

        // stats
        if (item.getMbxStats() != null) {
            int fieldW = (PANEL_W / 2 - PAD - 16) / 2;
            int minX = panelX + PANEL_W / 2;
            int maxX = minX + fieldW + 4 + font.width("to") + 4;
            for (String statName : item.getMbxStats().keySet()) {
                statRowYMap.put(statName, y);
                EditBox minField = new EditBox(font, minX, y + (ROW_H - INPUT_H) / 2, fieldW, INPUT_H, Component.empty());
                minField.setHint(Component.literal("Any"));
                minField.setMaxLength(12);
                addRenderableWidget(minField);

                EditBox maxField = new EditBox(font, maxX, y + (ROW_H - INPUT_H) / 2, fieldW, INPUT_H, Component.empty());
                maxField.setHint(Component.literal("Any"));
                maxField.setMaxLength(12);
                addRenderableWidget(maxField);

                statFields.put(statName, new EditBox[]{minField, maxField});
                y += ROW_H + 4;
            }
            if (!item.getMbxStats().isEmpty()) y += PAD;
        }

        // btn
        buttonsY = y;
        int btnW = (PANEL_W - PAD * 2 - 8) / 2;
        Button createBtn = Button.builder(Component.literal("Create Alert"), b -> onCreateAlert())
                .bounds(panelX + PAD, y, btnW, ROW_H)
                .build();
        addRenderableWidget(createBtn);

        Button cancelBtn = Button.builder(Component.literal("Cancel"), b -> this.minecraft.gui.setScreen(parent))
                .bounds(panelX + PAD + btnW + 8, y, btnW, ROW_H)
                .build();
        addRenderableWidget(cancelBtn);
        y += ROW_H + PAD;

        // alerts
        List<AhAlert> existing = Config.ahAlerts.getAlertsForItem(item.getId());
        if (!existing.isEmpty()) {
            y += 1 + 4; // divider
            alertsLabelY = y;
            y += font.lineHeight + 8;
            int deleteW = 20;
            for (int i = 0; i < existing.size(); i++) {
                alertRowYList.add(y);
                final int globalIdx = Config.ahAlerts.alerts.indexOf(existing.get(i));
                Button deleteBtn = Button.builder(Component.literal("×"), b -> {
                    Config.ahAlerts.removeAlert(globalIdx);
                    ConfigManager.save();
                    this.minecraft.gui.setScreen(new AhAlertEditScreen(item, parent));
                }).bounds(panelX + PANEL_W - PAD - deleteW, y + 2, deleteW, ROW_H - 4).build();
                addRenderableWidget(deleteBtn);
                y += ROW_H + 4;
            }
        }
    }

    private void onCreateAlert() {
        Long maxPrice = null;
        String priceText = priceField.getValue().trim().replace(",", "").replace(" ", "");
        if (!priceText.isEmpty()) {
            try {
                maxPrice = Long.parseLong(priceText);
            } catch (NumberFormatException ignored) {}
        }

        Map<String, AhAlert.StatFilter> filters = new LinkedHashMap<>();
        for (Map.Entry<String, EditBox[]> entry : statFields.entrySet()) {
            Long min = parseLong(entry.getValue()[0].getValue());
            Long max = parseLong(entry.getValue()[1].getValue());
            if (min != null || max != null) {
                filters.put(entry.getKey(), new AhAlert.StatFilter(min, max));
            }
        }

        Config.ahAlerts.addAlert(new AhAlert(item.getId(), maxPrice, filters.isEmpty() ? null : filters));
        ConfigManager.save();
        this.minecraft.gui.setScreen(parent);
    }

    private Long parseLong(String s) {
        s = s.trim().replace(",", "").replace(" ", "");
        if (s.isEmpty()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xAA000000);

        // panel
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH, 0xFF111111);
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + 1, 0xAAFFAA00);
        ctx.fill(panelX, panelY + panelH - 1, panelX + PANEL_W, panelY + panelH, 0xAAFFAA00);
        ctx.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xAAFFAA00);
        ctx.fill(panelX + PANEL_W - 1, panelY, panelX + PANEL_W, panelY + panelH, 0xAAFFAA00);

        // head (icon and name)
        int iconX = panelX + PAD;
        int iconY = panelY + PAD;
        Identifier tex = ItemListWidget.ItemEntry.getTexture(item.getId());
        if (tex == null && item.getTexture() != null) {
            tex = ItemListWidget.ItemEntry.loadTexture(item.getId(), item.getTexture());
        }
        if (tex != null) {
            float scale = ICON_SZ / 16.0f;
            Matrix3x2f backup = new Matrix3x2f(ctx.pose());
            ctx.pose().translate(iconX, iconY);
            ctx.pose().scale(scale, scale);
            ctx.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0, 0, 16, 16, 16, 16);
            ctx.pose().set(backup);
        }

        int textX = iconX + ICON_SZ + 10;
        ctx.text(font, MineboxItem.getDisplayName(item), textX, iconY + 4, 0xFFFFFFFF, true);
        int rarityColor = item.getRarity() != null
                ? RaritiesUtils.getRarityColor(item.getRarity().toLowerCase()).getRGB() | 0xFF000000
                : 0xFF888888;
        String meta = capitalize(item.getRarity() != null ? item.getRarity() : "") + " · Lvl " + item.getLevel();
        if (item.getCategory() != null) meta += " · " + capitalize(item.getCategory());
        ctx.text(font, Component.literal(meta), textX, iconY + 4 + font.lineHeight + 4, rarityColor, false);

        // div
        int divY = panelY + PAD + ICON_SZ + PAD;
        ctx.fill(panelX + PAD, divY, panelX + PANEL_W - PAD, divY + 1, 0x44FFFFFF);

        // price
        ctx.text(font, Component.literal("Max price ≤"),
                panelX + PAD, priceRowY + (ROW_H - font.lineHeight) / 2, 0xFFCCCCCC, false);

        // stats
        for (Map.Entry<String, Integer> e : statRowYMap.entrySet()) {
            String translationKey = e.getKey().replace("_", ".");
            Component statLabel = MineboxItem.getColoredStatName(translationKey);
            int labelY = e.getValue() + (ROW_H - font.lineHeight) / 2;
            ctx.text(font, statLabel, panelX + PAD, labelY, 0xFFCCCCCC, false);

            MineboxStat stat = item.getMbxStats() != null ? item.getMbxStats().get(e.getKey()) : null;
            if (stat != null && stat.getMin() != null && stat.getMax() != null) {
                String baseStr = " " + (stat.getMin().equals(stat.getMax())
                        ? stat.getMin()
                        : stat.getMin() + "–" + stat.getMax());
                int statColor = statLabel.getStyle().getColor() != null
                        ? statLabel.getStyle().getColor().getValue() | 0xFF000000
                        : 0xFF888888;
                int baseX = panelX + PAD + font.width(statLabel);
                final int fc = statColor;
                ctx.text(font, Component.literal(baseStr).withStyle(s -> s.withColor(fc)), baseX, labelY, fc, false);
            }

            EditBox[] fields = statFields.get(e.getKey());
            if (fields != null) {
                int toX = fields[0].getX() + fields[0].getWidth() + 4;
                ctx.text(font, Component.literal("to"), toX, labelY, 0xFF777777, false);
            }
        }

        // alerts
        List<AhAlert> existing = Config.ahAlerts.getAlertsForItem(item.getId());
        if (!existing.isEmpty()) {
            int adivY = buttonsY + ROW_H + PAD;
            ctx.fill(panelX + PAD, adivY, panelX + PANEL_W - PAD, adivY + 1, 0x44FFFFFF);

            ctx.text(font,
                    Component.literal("Active alerts:").withStyle(s -> s.withBold(true)),
                    panelX + PAD, alertsLabelY, 0xFFCCCCCC, false);

            for (int i = 0; i < existing.size() && i < alertRowYList.size(); i++) {
                int ry = alertRowYList.get(i);
                ctx.fill(panelX + PAD, ry, panelX + PANEL_W - PAD, ry + ROW_H - 4, 0x22FFFFFF);
                String desc = describeAlert(existing.get(i));
                ctx.text(font, Component.literal(desc), panelX + PAD + 4, ry + (ROW_H - 4 - font.lineHeight) / 2, 0xFFAAAAAA, false);
            }
        }

        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private String describeAlert(AhAlert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append(alert.maxPrice != null ? "≤ " + formatCoins(alert.maxPrice) : "Any price");
        if (alert.statFilters != null && !alert.statFilters.isEmpty()) {
            for (Map.Entry<String, AhAlert.StatFilter> e : alert.statFilters.entrySet()) {
                AhAlert.StatFilter f = e.getValue();
                sb.append("  ").append(e.getKey(), 0, Math.min(3, e.getKey().length())).append(": ");
                sb.append(f.min != null ? f.min : "0").append("–").append(f.max != null ? f.max : "∞");
            }
        }
        return sb.toString();
    }

    private String formatCoins(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000) return String.format("%.0fk", n / 1_000.0);
        return String.valueOf(n);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s != null ? s : "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public boolean shouldPause() {
        return false;
    }
}
