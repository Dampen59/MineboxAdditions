package io.dampen59.mineboxadditions.features.harvestable;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.config.other.HarvestablesSettings;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.IntConsumer;

public class HarvestableScreen extends Screen {
    private static final int PADDING = 8;
    private static final int ROW_H = 20;
    private static final int TAB_Y = PADDING + 12;
    private static final int TAB_H = 14;
    private static final int LIST_TOP = PADDING + 30;

    private final Minecraft mc = Minecraft.getInstance();

    private String forcedIslandKey = null;
    private List<String> availableWorlds = new ArrayList<>();
    private String islandKeyPath;
    private List<Harvestable> data = List.of();

    private final Map<String, List<Harvestable>> byCategory = new HashMap<>();
    private final Map<Harvestable, List<Harvestable>> groupMembers = new HashMap<>();

    private final Map<String, Checkbox> catChecks = new HashMap<>();
    private final Map<Harvestable, Checkbox> itemChecks = new HashMap<>();

    private final Map<Harvestable, Button> colorButtons = new HashMap<>();
    private final Map<Harvestable, Integer> currentColors = new HashMap<>();


    private int scrollY = 0;
    private int contentHeight = 0;

    private HarvestablesSettings.Harvestable prefs;

    public HarvestableScreen() {
        super(Component.literal("Harvestables"));
    }

    @Override
    protected void init() {
        super.init();

        Identifier worldId = mc.level != null ? mc.level.dimension().identifier()
                : Identifier.fromNamespaceAndPath("minecraft", "overworld");

        if (forcedIslandKey != null) {
            islandKeyPath = forcedIslandKey;
        } else {
            islandKeyPath = worldId.getPath();
        }

        availableWorlds = new ArrayList<>(MineboxAdditions.INSTANCE.state.getAllHarvestableKeys());
        Collections.sort(availableWorlds);

        prefs = Config.harvestables.harvestables.computeIfAbsent(islandKeyPath, k -> new HarvestablesSettings.Harvestable());

        var modState = MineboxAdditions.INSTANCE.state;
        List<Harvestable> raw = modState.getMineboxHarvestables(islandKeyPath);
        if (raw == null || raw.isEmpty() && forcedIslandKey == null)
            raw = modState.getMineboxHarvestables(worldId.toString());
        data = raw != null ? raw : List.of();

        Map<String, Map<String, List<Harvestable>>> grouped = new HashMap<>();
        for (var h : data) {
            String cat = h.getCategory() != null ? h.getCategory() : "misc";
            String name = h.getName() != null ? h.getName() : "unknown";
            grouped.computeIfAbsent(cat, k -> new HashMap<>())
                    .computeIfAbsent(name, k -> new ArrayList<>())
                    .add(h);
        }

        byCategory.clear();
        groupMembers.clear();
        for (var e : grouped.entrySet()) {
            String cat = e.getKey();
            List<Harvestable> reps = new ArrayList<>();
            for (var nameEntry : e.getValue().entrySet()) {
                List<Harvestable> members = nameEntry.getValue();
                Harvestable rep = members.get(0);
                reps.add(rep);
                groupMembers.put(rep, members);
            }
            reps.sort(Comparator.comparing(h -> h.getName() == null ? "" : h.getName()));
            byCategory.put(cat, reps);
        }

        preloadAllHarvestableTextures();

        this.clearWidgets();
        catChecks.clear();
        itemChecks.clear();
        colorButtons.clear();
        currentColors.clear();

        int startY = LIST_TOP;
        int curY = startY;

        for (String cat : sortedCategories()) {
            boolean catChecked = prefs.categories.getOrDefault(cat, true);

            Checkbox catCb = Checkbox.builder(Component.literal(cat), this.font)
                    .pos(PADDING + 16, curY)
                    .selected(catChecked)
                    .onValueChange((cb, checked) -> {
                        prefs.categories.put(cat, checked);
                        ConfigManager.save();
                    })
                    .build();
            addRenderableWidget(catCb);
            catChecks.put(cat, catCb);
            curY += ROW_H;

            for (var rep : byCategory.get(cat)) {
                String name = rep.getName() != null ? rep.getName() : "unknown";
                boolean itemChecked = prefs.items
                        .getOrDefault(cat, Collections.emptyMap())
                        .getOrDefault(name, true);

                int savedColor = prefs.colors
                        .getOrDefault(cat, Collections.emptyMap())
                        .getOrDefault(name, 0xFFFFFFFF); // default white

                int count = groupMembers.getOrDefault(rep, List.of()).size();

                Checkbox itCb = Checkbox.builder(Component.literal(""), this.font)
                        .pos(PADDING + 36, curY)
                        .selected(itemChecked)
                        .onValueChange((cb, checked) -> {
                            prefs.items
                                    .computeIfAbsent(cat, k -> new HashMap<>())
                                    .put(name, checked);
                            ConfigManager.save();
                        })
                        .build();

                addRenderableWidget(itCb);
                itemChecks.put(rep, itCb);

                // btn set color
                Button swatch = Button.builder(Component.literal(""), b -> {
                            this.minecraft.gui.setScreen(new ColorPickerScreen(
                                    savedColorOrCurrent(rep),
                                    picked -> {
                                        currentColors.put(rep, picked);
                                        prefs.colors
                                                .computeIfAbsent(cat, k -> new HashMap<>())
                                                .put(name, picked);
                                        ConfigManager.save();
                                        this.minecraft.gui.setScreen(this);
                                    },
                                    this
                            ));
                        })
                        .bounds(this.width - PADDING - 18, curY + 3, 14, 14)
                        .build();
                addRenderableWidget(swatch);
                colorButtons.put(rep, swatch);
                currentColors.put(rep, savedColor);

                curY += ROW_H;
            }
        }
        contentHeight = curY - startY;
    }

    private List<String> sortedCategories() {
        ArrayList<String> keys = new ArrayList<>(byCategory.keySet());
        Collections.sort(keys);
        return keys;
    }

    private int savedColorOrCurrent(Harvestable rep) {
        return currentColors.getOrDefault(rep, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int listTop = LIST_TOP;
        int listBottom = this.height - PADDING - 26;
        int listH = Math.max(0, listBottom - listTop);
        int maxScroll = Math.max(0, contentHeight - listH);

        if (mouseY >= listTop && mouseY <= listBottom && maxScroll > 0) {
            scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - vertical * 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
        if (!availableWorlds.isEmpty()) {
            int tx = PADDING;
            for (String world : availableWorlds) {
                Component label = formatWorldName(world);
                int tw = this.font.width(label) + 10;
                boolean selected = world.equals(islandKeyPath);
                boolean hovered = mouseX >= tx && mouseX < tx + tw
                               && mouseY >= TAB_Y && mouseY < TAB_Y + TAB_H;
                int bg = selected ? 0xFF607090 : (hovered ? 0xFF404060 : 0xFF2A2A3A);
                draw.fill(tx, TAB_Y, tx + tw, TAB_Y + TAB_H, bg);
                if (selected) draw.fill(tx, TAB_Y + TAB_H - 2, tx + tw, TAB_Y + TAB_H, 0xFFAABBFF);
                draw.text(this.font, label, tx + 5, TAB_Y + 3, 0xFFFFFFFF, false);
                tx += tw + 4;
            }
        }

        int listTop = LIST_TOP;
        int listBottom = this.height - PADDING - 26;
        int listLeft = PADDING;
        int listRight = this.width - PADDING;

        draw.fill(listLeft - 2, listTop - 2, listRight + 2, listBottom + 2, 0x80000000);

        draw.enableScissor(listLeft, listTop, listRight, listBottom);

        int y = listTop - scrollY;
        for (String cat : sortedCategories()) {
            Checkbox catCb = catChecks.get(cat);
            if (catCb != null) catCb.setPosition(listLeft + 16, y);
            y += ROW_H;

            for (var rep : byCategory.getOrDefault(cat, List.of())) {
                Checkbox itCb = itemChecks.get(rep);
                if (itCb != null) itCb.setPosition(listLeft + 36, y);

                Button sw = colorButtons.get(rep);
                if (sw != null) sw.setPosition(listRight - 18, y + 3);

                y += ROW_H;
            }
        }

        super.extractRenderState(draw, mouseX, mouseY, delta);

        {
            int rowY = listTop - scrollY;

            for (String cat : sortedCategories()) {
                rowY += ROW_H;

                for (var rep : byCategory.getOrDefault(cat, List.of())) {
                    int cbX = listLeft + 36;
                    int afterCb = cbX + 20;
                    int iconX = afterCb + 4;
                    int iconY = rowY + 2;

                    String id = rep.getName() != null ? rep.getName() : "unknown";
                    var item = MineboxAdditions.INSTANCE.state.getItemById(id);

                    Identifier icon = ItemListWidget.ItemEntry.getTexture(id);
                    if (icon == null) {
                        icon = resolveHarvestableIcon(id);
                        if (icon != null) {
                            ItemListWidget.ItemEntry.getTextureCache().put(id, icon);
                            if (item != null) preloadIngredientTextures(item);
                        }
                    }

                    if (icon != null) {
                        draw.blit(
                                RenderPipelines.GUI_TEXTURED,
                                icon,
                                iconX, iconY,
                                0, 0,
                                16, 16,
                                16, 16
                        );
                    }

                    int textX = iconX + (icon != null ? 16 + 4 : 0);
                    int nameY = rowY + 4;

                    Component nameText = (item != null)
                            ? MineboxItem.getDisplayName(item)
                            : Component.translatable("mbx.harvestables." + id + ".name");

                    int lvl = rep.getRequiredLevel();
                    if (lvl > 0) {
                        nameText = Component.empty().append(nameText).append(" (Lvl. " + lvl + ")");
                    }

                    int nameColor = 0xFFFFFFFF;
                    if (item != null && item.getRarity() != null) {
                        nameColor = RaritiesUtils
                                .getRarityColor(item.getRarity().toLowerCase())
                                .getRGB() | 0xFF000000;
                    }

                    draw.text(this.font, nameText, textX, nameY, nameColor, false);

                    int nameW = this.font.width(nameText);
                    int cnt = groupMembers.getOrDefault(rep, List.of()).size();
                    String cntStr = " (" + cnt + ")";
                    draw.text(this.font, cntStr, textX + nameW, nameY, 0xFFAAAAAA, false);

                    rowY += ROW_H;
                }
            }
        }

        for (var e : colorButtons.entrySet()) {
            Button sw = e.getValue();
            int col = currentColors.getOrDefault(e.getKey(), 0xFFFFFFFF) | 0xFF000000;
            String hash = "#";
            int tw = this.font.width(hash);
            int th = this.font.lineHeight;
            int cx = sw.getX() + (sw.getWidth() - tw) / 2;
            int cy = sw.getY() + (sw.getHeight() - th) / 2 + 1;
            draw.text(this.font, hash, cx, cy, col, false);
        }

        draw.disableScissor();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.y() >= TAB_Y && event.y() < TAB_Y + TAB_H) {
            int tx = PADDING;
            for (String world : availableWorlds) {
                int tw = mc.font.width(formatWorldName(world)) + 10;
                if (event.x() >= tx && event.x() < tx + tw) {
                    forcedIslandKey = world;
                    scrollY = 0;
                    rebuildWidgets();
                    return true;
                }
                tx += tw + 4;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static Component formatWorldName(String key) {
        return Component.translatable("mineboxadditions.strings.zones." + key);
    }

    public void close() {
        onClose();
    }

    public boolean shouldPause() {
        return false;
    }


    private static class ColorPickerScreen extends Screen {
        private final int initialColor;
        private final IntConsumer onPicked;
        private final Screen parent;

        private EditBox hexField;
        private int color; // 0xRRGGBB

        private final List<Button> presetButtons = new ArrayList<>();
        private final Map<Button, Integer> presetColors = new HashMap<>();

        protected ColorPickerScreen(int initialColor, IntConsumer onPicked, Screen parent) {
            super(Component.literal("Pick Color"));
            this.initialColor = initialColor & 0xFFFFFF;
            this.color = this.initialColor;
            this.onPicked = onPicked;
            this.parent = parent;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int y = 40;

            hexField = new EditBox(this.font, cx - 60, y, 120, 20, Component.literal("Hex"));
            hexField.setMaxLength(7);
            hexField.setValue("#" + toHex(color));
            hexField.setResponder(this::onHexChanged);
            this.addRenderableWidget(hexField);
            y += 26;

            // waypoints xaero inspired lol
            int[] presets = new int[] {
                0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF,
                0xFFFF00, 0x00FFFF, 0xFF00FF, 0xFFA500
            };
            int startX = cx - (presets.length * 22) / 2;
            for (int i = 0; i < presets.length; i++) {
                final int c = presets[i];
                Button presetBtn = Button.builder(Component.literal(""), b -> {
                            color = c;
                            hexField.setValue("#" + toHex(color));
                        })
                        .bounds(startX + i * 22, y, 18, 18)
                        .build();
                this.addRenderableWidget(presetBtn);
                presetButtons.add(presetBtn);
                presetColors.put(presetBtn, c);
            }
            y += 28;

            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
                        onPicked.accept(color);
                        this.close();
                    }).bounds(cx - 80, y, 70, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
                        this.minecraft.gui.setScreen(parent);
                    }).bounds(cx + 10, y, 70, 20).build());
        }

        private void onHexChanged(String text) {
            int parsed = parseHexPartial(text, color);
            if (parsed != -1) {
                color = parsed;
            }
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
            //this.renderBackground(draw, mouseX, mouseY, delta);
            int cx = this.width / 2;

            draw.text(this.font, "Pick Beam Color", cx - 50, 16, 0xFFFFFF, false);

            int px0 = cx - 40, py0 = 20, px1 = cx + 40, py1 = 36;
            int col = 0xFF000000 | color;
            draw.fill(px0 - 1, py0 - 1, px1 + 1, py1 + 1, 0xFF000000);
            draw.fill(px0, py0, px1, py1, col);

            super.extractRenderState(draw, mouseX, mouseY, delta);

            for (Button btn : presetButtons) {
                int c = presetColors.getOrDefault(btn, 0xFFFFFFFF) | 0xFF000000;
                String hash = "#";
                int tw = this.font.width(hash);
                int th = this.font.lineHeight;
                int cxTxt = btn.getX() + (btn.getWidth() - tw) / 2;
                int cyTxt = btn.getY() + (btn.getHeight() - th) / 2 + 1;
                draw.text(this.font, hash, cxTxt, cyTxt, c, false);
            }
        }

        public void close() {
            this.minecraft.gui.setScreen(parent);
        }

        private static String toHex(int rgb) {
            String s = Integer.toHexString(rgb & 0xFFFFFF).toUpperCase(Locale.ROOT);
            while (s.length() < 6) s = "0" + s;
            return s;
        }

        // This func comes from StackOverflow, so thanks to the OG author
        private static int parseHexPartial(String text, int fallback) {
            if (text == null) return -1;
            String t = text.trim();
            if (t.startsWith("#")) t = t.substring(1);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < t.length(); i++) {
                char ch = t.charAt(i);
                if ((ch >= '0' && ch <= '9') ||
                    (ch >= 'a' && ch <= 'f') ||
                    (ch >= 'A' && ch <= 'F')) {
                    sb.append(ch);
                }
            }
            String hex = sb.toString();
            if (hex.isEmpty()) return -1;

            if (hex.length() == 3) {
                char r = hex.charAt(0), g = hex.charAt(1), b = hex.charAt(2);
                hex = "" + r + r + g + g + b + b;
            } else {
                if (hex.length() > 6) hex = hex.substring(0, 6);
                if (hex.length() < 6) {
                    hex = String.format(Locale.ROOT, "%-6s", hex).replace(' ', '0');
                }
            }
            try {
                return Integer.parseInt(hex, 16) & 0xFFFFFF;
            } catch (Exception e) {
                return fallback & 0xFFFFFF;
            }
        }
    }

    private static String idOf(Harvestable h) {
        String n = h.getName();
        return (n != null && !n.isEmpty()) ? n : "unknown";
    }

    private static Identifier resolveHarvestableIcon(String id) {
        Identifier cached = ItemListWidget.ItemEntry.getTexture(id);
        if (cached != null) return cached;

        MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
        if (item != null) {
            String b64 = item.getTexture();
            if (b64 != null && !b64.isEmpty()) {
                try {
                    String textureName = "textures/items/" + id + ".png";
                    return ImageUtils.createTextureFromBase64(b64, textureName);
                } catch (Exception e) {
                    System.err.println("Failed to decode texture for harvestable " + id);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static void preloadIngredientTextures(MineboxItem item) {
        if (item.getRecipe() == null || item.getRecipe().getIngredients() == null) return;

        Map<String, Identifier> cache = ItemListWidget.ItemEntry.getTextureCache();

        for (MineboxItem.Ingredient ing : item.getRecipe().getIngredients()) {
            if (!ing.isVanilla()) {
                MineboxItem sub = ing.getCustomItem();
                if (sub != null) {
                    String sid = sub.getId();
                    if (!cache.containsKey(sid)) {
                        try {
                            String textureName = "textures/items/" + sid + ".png";
                            Identifier icon = ImageUtils.createTextureFromBase64(sub.getTexture(), textureName);
                            if (icon != null) cache.put(sid, icon);
                        } catch (Exception e) {
                            System.err.println("Failed to decode ingredient texture for " + sid);
                            e.printStackTrace();
                        }
                    }
                    preloadIngredientTextures(sub);
                }
            } else {
                String vid = ing.getId();
                cache.putIfAbsent(vid, Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + vid + ".png"));
            }
        }
    }


    private void preloadAllHarvestableTextures() {
        Map<String, Identifier> cache = ItemListWidget.ItemEntry.getTextureCache();

        for (var reps : byCategory.values()) {
            for (Harvestable rep : reps) {
                String id = rep.getName();
                if (id == null || id.isEmpty()) continue;
                if (!cache.containsKey(id)) {
                    Identifier icon = resolveHarvestableIcon(id);
                    if (icon != null) {
                        cache.put(id, icon);
                        MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
                        if (item != null) preloadIngredientTextures(item);
                    }
                }
            }
        }
    }

}