package io.dampen59.mineboxadditions.features.bestiary;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;

public class BestiaryScreen extends Screen {

    private static final int GIF_SIZE = 256;
    private static final int DETAIL_IMG_SIZE = 128;
    private static final int LIST_WIDTH = 200;

    private BestiaryListWidget entryList;
    private EditBox searchField;
    private List<BestiaryEntry> allEntries;
    private BestiaryEntry selected = null;
    private int detailScrollY = 0;

    private final String preSelectEntryId;
    private final List<DropRegion> dropRegions = new ArrayList<>();

    public BestiaryScreen() {
        this(null);
    }

    public BestiaryScreen(String preSelectEntryId) {
        super(Component.literal("Bestiary"));
        this.preSelectEntryId = preSelectEntryId;
    }

    @Override
    protected void init() {
        Font font = Minecraft.getInstance().font;
        int left = 10;
        int listTop = 45;
        int listHeight = this.height - listTop - 10;

        searchField = new EditBox(font, left, 15, LIST_WIDTH, 20, Component.literal("Search..."));
        searchField.setResponder(this::updateFilteredEntries);
        this.addRenderableWidget(searchField);

        entryList = new BestiaryListWidget(minecraft, left, listTop, LIST_WIDTH, listHeight, 28);
        this.addRenderableWidget(entryList);

        allEntries = MineboxAdditions.INSTANCE.state.getMbxBestiary() != null
            ? new ArrayList<>(MineboxAdditions.INSTANCE.state.getMbxBestiary())
            : new ArrayList<>();

        updateFilteredEntries(searchField.getValue());

        if (preSelectEntryId != null) {
            allEntries.stream()
                .filter(e -> e.getId().equals(preSelectEntryId))
                .findFirst()
                .ifPresent(this::setSelected);
        }
    }

    private void updateFilteredEntries(String query) {
        entryList.clearEntries();
        entryList.setScrollAmount(0);
        String q = query.toLowerCase();
        for (BestiaryEntry e : allEntries) {
            if (q.isEmpty()
                || e.getId().toLowerCase().contains(q)
                || e.getName().toLowerCase().contains(q)
                || e.getFamily().toLowerCase().contains(q)
                || (e.getZones() != null && e.getZones().stream().anyMatch(z -> z.toLowerCase().contains(q)))) {
                entryList.addEntry(new BestiaryListWidget.EntryRow(e, this));
            }
        }
    }

    public BestiaryEntry getSelected() { return selected; }

    public void setSelected(BestiaryEntry entry) {
        this.selected = entry;
        this.detailScrollY = 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
        dropRegions.clear();
        super.extractRenderState(draw, mouseX, mouseY, delta);

        int pad = 8;
        int detailPanelX = pad + LIST_WIDTH + 4 + 8;
        draw.fill(detailPanelX, pad, this.width - pad, this.height - pad, 0x80000000);

        if (selected == null) return;

        Font font = Minecraft.getInstance().font;
        int detailX = 10 + LIST_WIDTH + 20;
        int detailTop = 10;
        int detailW = this.width - detailX - 10;
        int detailBottom = this.height - 10;
        int lineH = font.lineHeight + 3;

        draw.enableScissor(detailX, detailTop, detailX + detailW, detailBottom);

        int x = detailX;
        int y = detailTop - detailScrollY;

        Identifier icon = BestiaryListWidget.EntryRow.loadAndCacheTexture(selected);
        if (icon != null) {
            int imgX = x + (detailW - DETAIL_IMG_SIZE) / 2;
            Matrix3x2f backup = new Matrix3x2f(draw.pose());
            draw.pose().translate(imgX, y);
            draw.pose().scale((float) DETAIL_IMG_SIZE / GIF_SIZE, (float) DETAIL_IMG_SIZE / GIF_SIZE);
            draw.blit(RenderPipelines.GUI_TEXTURED, icon, 0, 0, 0, 0, GIF_SIZE, GIF_SIZE, GIF_SIZE, GIF_SIZE);
            draw.pose().set(backup);
            y += DETAIL_IMG_SIZE + 8;
        }

        Component name = Component.literal(selected.getName());
        draw.text(font, name, x + (detailW - font.width(name)) / 2, y, 0xFFFFFFFF, true);
        y += lineH;

        Component sub = Component.literal(capitalize(selected.getFamily()) + " • " + selected.getType());
        draw.text(font, sub, x + (detailW - font.width(sub)) / 2, y, 0xFFAAAAAA, false);
        y += lineH + 6;

        String levelStr = selected.getLevelMax() > selected.getLevel()
            ? "Level: " + selected.getLevel() + " → " + selected.getLevelMax()
            : "Level: " + selected.getLevel();
        draw.text(font, Component.literal(levelStr), x, y, 0xFFCCCCCC, false);
        y += lineH;

        if (selected.getHealth() != null && selected.getHealth().size() >= 2) {
            int hpMin = selected.getHealth().get(0), hpMax = selected.getHealth().get(1);
            String hpStr = hpMin == hpMax ? "HP: " + hpMin : "HP: " + hpMin + " → " + hpMax;
            draw.text(font, Component.literal(hpStr), x, y, 0xFFFF6666, false);
            y += lineH;
        }

        if (selected.getZones() != null && !selected.getZones().isEmpty()) {
            y += 4;
            draw.text(font, Component.literal("Zones"), x, y, 0xFFAAAAFF, false);
            y += lineH;
            for (String zone : selected.getZones()) {
                draw.text(font, Component.literal("  ").append(Component.translatable("mineboxadditions.strings.zones." + zone)),
                    x, y, 0xFFCCCCCC, false);
                y += lineH;
            }
        }

        if (selected.getDrops() != null && !selected.getDrops().isEmpty()) {
            y += 4;
            draw.text(font, Component.literal("Drops"), x, y, 0xFFFFAA00, false);
            y += lineH;

            for (BestiaryDrop drop : selected.getDrops()) {
                String itemId = drop.getItemId();
                Identifier dropIcon = resolveItemTexture(itemId);

                int iconSize = 16;
                int textOffsetX = x;

                if (dropIcon != null) {
                    draw.blit(RenderPipelines.GUI_TEXTURED, dropIcon, x, y + 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
                    textOffsetX = x + iconSize + 3;
                }

                Component itemName = resolveItemName(itemId);
                boolean inAtlas = MineboxAdditions.INSTANCE.state.getItemById(itemId) != null;
                int nameColor = inAtlas ? 0xFFFFFFFF : 0xFFAAAAAA;
                draw.text(font, itemName, textOffsetX, y + (iconSize - font.lineHeight) / 2, nameColor, false);

                if (inAtlas) {
                    dropRegions.add(new DropRegion(itemId, x, y, detailW, iconSize + 2));
                }
                y += iconSize + 2;

                String dropInfo = "    x" + formatRange(drop.getAmount()) + "  " + formatChance(drop.getChance());
                draw.text(font, Component.literal(dropInfo), x, y, 0xFFAAAAAA, false);
                y += lineH;
            }
        }

        if (selected.getStats() != null && !selected.getStats().isEmpty()) {
            y += 4;
            draw.text(font, Component.literal("Stats"), x, y, 0xFF66FF66, false);
            y += lineH;
            for (var entry : selected.getStats().entrySet()) {
                draw.text(font,
                    Component.literal("  " + capitalize(entry.getKey()) + ": " + formatRange(entry.getValue())),
                    x, y, 0xFFCCCCCC, false);
                y += lineH;
            }
        }

        if (selected.getResistances() != null && !selected.getResistances().isEmpty()) {
            y += 4;
            draw.text(font, Component.literal("Resistances"), x, y, 0xFF6699FF, false);
            y += lineH;
            for (var entry : selected.getResistances().entrySet()) {
                draw.text(font,
                    Component.literal("  " + capitalize(entry.getKey()) + ": " + formatResistance(entry.getValue())),
                    x, y, resistanceColor(entry.getValue()), false);
                y += lineH;
            }
        }

        draw.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();

        for (DropRegion r : dropRegions) {
            if (r.contains(mx, my)) {
                navigateToAtlas(r.itemId());
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void navigateToAtlas(String itemId) {
        List<MineboxItem> items = MineboxAdditions.INSTANCE.state.getMbxItems();
        if (items == null || items.isEmpty()) return;
        this.minecraft.gui.setScreen(new MineboxAtlasScreen(itemId));
    }

    private Identifier resolveItemTexture(String itemId) {
        Identifier icon = ItemListWidget.ItemEntry.getTexture(itemId);
        if (icon == null) {
            MineboxItem atlasItem = MineboxAdditions.INSTANCE.state.getItemById(itemId);
            if (atlasItem != null) {
                icon = ItemListWidget.ItemEntry.getTextureCache()
                    .computeIfAbsent(itemId, id -> ItemListWidget.ItemEntry.loadTexture(id, atlasItem.getTexture()));
            }
        }
        return icon;
    }

    private Component resolveItemName(String itemId) {
        List<MineboxItem> items = MineboxAdditions.INSTANCE.state.getMbxItems();
        if (items != null) {
            MineboxItem found = items.stream().filter(i -> i.getId().equals(itemId)).findFirst().orElse(null);
            if (found != null) return MineboxItem.getDisplayName(found);
        }
        return Component.translatable("mbx.items." + itemId + ".name");
    }

    private static String formatRange(List<Integer> range) {
        if (range == null || range.isEmpty()) return "?";
        if (range.size() == 1 || range.get(0).equals(range.get(1))) return String.valueOf(range.get(0));
        return range.get(0) + " - " + range.get(1);
    }

    private static String formatChance(double chance) {
        if (chance == Math.floor(chance)) return "(" + (int) chance + "%)";
        return "(" + String.format("%.1f%%", chance) + ")";
    }

    private static String formatResistance(List<Integer> vals) {
        if (vals == null || vals.isEmpty()) return "?";
        int lo = vals.get(0), hi = vals.size() >= 2 ? vals.get(1) : lo;
        return lo == hi ? lo + "%" : lo + "% → " + hi + "%";
    }

    private static int resistanceColor(List<Integer> vals) {
        if (vals == null || vals.isEmpty()) return 0xFFCCCCCC;
        int v = vals.get(0);
        if (v > 0) return 0xFF66FF66;
        if (v < 0) return 0xFFFF6666;
        return 0xFFCCCCCC;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).replace('_', ' ');
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int detailX = 10 + LIST_WIDTH + 20;
        if (mouseX > detailX && selected != null) {
            detailScrollY = Math.max(0, (int) (detailScrollY - vertical * 12));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private record DropRegion(String itemId, int x, int y, int w, int h) {
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
