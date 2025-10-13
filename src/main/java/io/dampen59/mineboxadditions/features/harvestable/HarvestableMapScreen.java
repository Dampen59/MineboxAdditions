package io.dampen59.mineboxadditions.features.harvestable;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.config.other.HarvestablesSettings;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.harvestable.render.BrowserTexture;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.IntConsumer;

public class HarvestableMapScreen extends Screen {

    private static final int PADDING = 8;
    private static final int ROW_H = 20;
    private static final int SIDEBAR_WIDTH = 320;

    private static final String BASE_URL = "http://localhost:8100/";
    private static final String MAP_HASH = "#island_main:175:0:-28:500:0:0:0:1:flat";

    protected BrowserTexture browserTexture;
    private MCEFBrowser browser = null;
    private static final Identifier MCEF_TEX_ID = Identifier.of("mineboxadditions", "mceftex");

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private String islandKeyPath;

    private final Map<String, List<Harvestable>> byCategory = new HashMap<>();
    private final Map<Harvestable, List<Harvestable>> groupMembers = new HashMap<>();

    private final Map<String, CheckboxWidget> catChecks = new HashMap<>();
    private final Map<Harvestable, CheckboxWidget> itemChecks = new HashMap<>();

    private final Map<Harvestable, ButtonWidget> colorButtons = new HashMap<>();
    private final Map<Harvestable, Integer> currentColors = new HashMap<>();

    private int scrollY = 0;
    private int contentHeight = 0;

    private HarvestablesSettings.Harvestable prefs;

    private final Map<String, Boolean> lastCatState = new HashMap<>();
    private final Map<Harvestable, Boolean> lastItemState = new HashMap<>();
    private final Map<Harvestable, String> repToCategory = new HashMap<>();

    public HarvestableMapScreen() {
        super(Text.literal("Harvestables Map"));
    }

    @Override
    protected void init() {
        super.init();

        Identifier worldId = mc.world != null ? mc.world.getRegistryKey().getValue()
                : Identifier.of("minecraft", "overworld");
        islandKeyPath = worldId.getPath();

        prefs = Config.harvestables.harvestables.computeIfAbsent(islandKeyPath, k -> new HarvestablesSettings.Harvestable());

        var modState = MineboxAdditions.INSTANCE.state;
        List<Harvestable> raw = modState.getMineboxHarvestables(islandKeyPath);
        if (raw == null || raw.isEmpty())
            raw = modState.getMineboxHarvestables(worldId.toString());
        List<Harvestable> data = raw != null ? raw : List.of();

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
                Harvestable rep = members.getFirst();
                reps.add(rep);
                groupMembers.put(rep, members);
            }
            reps.sort(Comparator.comparing(h -> h.getName() == null ? "" : h.getName()));
            byCategory.put(cat, reps);
        }

        preloadAllHarvestableTextures();

        this.clearChildren();
        catChecks.clear();
        itemChecks.clear();
        colorButtons.clear();
        currentColors.clear();

        int startY = PADDING;
        int curY = startY;

        for (String cat : sortedCategories()) {
            boolean catChecked = prefs.categories.getOrDefault(cat, true);

            CheckboxWidget catCb = CheckboxWidget.builder(Text.literal(cat), this.textRenderer)
                    .pos(PADDING + 16, curY)
                    .checked(catChecked)
                    .build();
            addDrawableChild(catCb);
            catChecks.put(cat, catCb);
            lastCatState.put(cat, catChecked);
            curY += ROW_H;

            for (var rep : byCategory.get(cat)) {
                String name = rep.getName() != null ? rep.getName() : "unknown";
                boolean itemChecked = prefs.items
                        .getOrDefault(cat, Collections.emptyMap())
                        .getOrDefault(name, true);

                int savedColor = prefs.colors
                        .getOrDefault(cat, Collections.emptyMap())
                        .getOrDefault(name, 0xFFFFFFFF);

                CheckboxWidget itCb = CheckboxWidget.builder(Text.literal(""), this.textRenderer)
                        .pos(PADDING + 36, curY)
                        .checked(itemChecked)
                        .build();

                addDrawableChild(itCb);
                itemChecks.put(rep, itCb);
                repToCategory.put(rep, cat);
                lastItemState.put(rep, itemChecked);

                ButtonWidget swatch = ButtonWidget.builder(Text.literal(""), b -> {
                            this.client.setScreen(new ColorPickerScreen(
                                    savedColorOrCurrent(rep),
                                    picked -> {
                                        currentColors.put(rep, picked);
                                        prefs.colors
                                                .computeIfAbsent(cat, k -> new HashMap<>())
                                                .put(name, picked);
                                        this.client.setScreen(this);
                                    },
                                    this
                            ));
                        })
                        .dimensions(SIDEBAR_WIDTH - PADDING - 18, curY + 3, 14, 14)
                        .build();
                addDrawableChild(swatch);
                colorButtons.put(rep, swatch);
                currentColors.put(rep, savedColor);

                curY += ROW_H;
            }
        }
        contentHeight = curY - startY;

        String fullUrl = buildInitialUrl();
        if (browser == null) {
            browserTexture = new BrowserTexture(-1, "mceftex");
            MCEF.getSettings().setUseCache(false);
            browser = MCEF.createBrowser(fullUrl, false);
            resizeBrowser();
            mc.getTextureManager().registerTexture(MCEF_TEX_ID, this.browserTexture);
        } else {
            browser.loadURL(fullUrl);
        }
    }

    @Override
    public void tick() {
        super.tick();

        for (Map.Entry<String, CheckboxWidget> e : catChecks.entrySet()) {
            String cat = e.getKey();
            boolean cur = e.getValue().isChecked();
            Boolean prev = lastCatState.get(cat);
            if (prev == null || prev != cur) {
                lastCatState.put(cat, cur);
                applyCategoryVisibilityToItems(cat, cur);
            }
        }

        for (Map.Entry<Harvestable, CheckboxWidget> e : itemChecks.entrySet()) {
            Harvestable rep = e.getKey();
            boolean cur = e.getValue().isChecked();
            Boolean prev = lastItemState.get(rep);
            if (prev == null || prev != cur) {
                lastItemState.put(rep, cur);

                String id = rep.getName() != null ? rep.getName() : "unknown";
                String cat = repToCategory.get(rep);
                boolean catChecked = true;
                CheckboxWidget catCb = (cat != null) ? catChecks.get(cat) : null;
                if (catCb != null) catChecked = catCb.isChecked();

                boolean shouldShow = catChecked && cur;
                js("if (window.mbxSetItem) { window.mbxSetItem(" + jsString(id) + ", " + shouldShow + "); }");
            }
        }
    }

    @Override
    public void resize(MinecraftClient minecraft, int w, int h) {
        super.resize(minecraft, w, h);
        resizeBrowser();
    }

    @Override
    public void close() {
        HarvestablesSettings.Harvestable p = Config.harvestables.harvestables.computeIfAbsent(islandKeyPath, k -> new HarvestablesSettings.Harvestable());
        p.categories.clear();
        for (String cat : byCategory.keySet()) {
            CheckboxWidget cb = catChecks.get(cat);
            if (cb != null) p.categories.put(cat, cb.isChecked());
        }
        p.items.clear();
        p.colors.clear();
        for (String cat : byCategory.keySet()) {
            Map<String, Boolean> itemsChecked = new HashMap<>();
            Map<String, Integer> itemsColor = new HashMap<>();
            for (var rep : byCategory.get(cat)) {
                String name = rep.getName() != null ? rep.getName() : "unknown";
                CheckboxWidget cb = itemChecks.get(rep);
                if (cb != null) itemsChecked.put(name, cb.isChecked());
                int col = currentColors.getOrDefault(rep, 0xFFFFFFFF);
                itemsColor.put(name, col);
            }
            p.items.put(cat, itemsChecked);
            p.colors.put(cat, itemsColor);
        }
        ConfigManager.save();

        if (browser != null) browser.close();
        super.close();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateFrame();
        int drawW = browserWidth();
        int drawH = browserHeight();
        if (drawW > 0 && drawH > 0) {
            ctx.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    MCEF_TEX_ID,
                    browserLeft(), browserTop(),
                    0f, 0f,
                    drawW, drawH,
                    drawW, drawH,
                    0xFFFFFFFF
            );
        }
        int listTop = PADDING;
        int listBottom = this.height - PADDING;
        int listLeft = PADDING;
        int listRight = SIDEBAR_WIDTH - PADDING;

        ctx.fill(listLeft - 2, listTop - 2, listRight + 2, listBottom + 2, 0x80000000);
        ctx.enableScissor(listLeft, listTop, listRight, listBottom);

        int y = listTop - scrollY;
        for (String cat : sortedCategories()) {
            CheckboxWidget catCb = catChecks.get(cat);
            if (catCb != null) catCb.setPosition(listLeft + 16, y);
            y += ROW_H;

            for (var rep : byCategory.getOrDefault(cat, List.of())) {
                CheckboxWidget itCb = itemChecks.get(rep);
                if (itCb != null) itCb.setPosition(listLeft + 36, y);

                ButtonWidget sw = colorButtons.get(rep);
                if (sw != null) sw.setPosition(listRight - 18, y + 3);

                y += ROW_H;
            }
        }

        super.render(ctx, mouseX, mouseY, delta);

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
                    ctx.drawTexture(
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

                Text nameText = (item != null)
                        ? MineboxItem.getDisplayName(item)
                        : Text.translatable("mbx.harvestables." + id + ".name");

                int nameColor = 0xFFFFFFFF;
                if (item != null && item.getRarity() != null) {
                    nameColor = RaritiesUtils
                            .getRarityColor(item.getRarity().toLowerCase())
                            .getRGB() | 0xFF000000;
                }

                ctx.drawText(this.textRenderer, nameText, textX, nameY, nameColor, false);

                int nameW = this.textRenderer.getWidth(nameText);
                int cnt = groupMembers.getOrDefault(rep, List.of()).size();
                String cntStr = " (" + cnt + ")";
                ctx.drawText(this.textRenderer, cntStr, textX + nameW, nameY, 0xFFAAAAAA, false);

                rowY += ROW_H;
            }
        }

        for (var e : colorButtons.entrySet()) {
            ButtonWidget sw = e.getValue();
            int col = currentColors.getOrDefault(e.getKey(), 0xFFFFFFFF) | 0xFF000000;
            String hash = "#";
            int tw = this.textRenderer.getWidth(hash);
            int th = this.textRenderer.fontHeight;
            int cx = sw.getX() + (sw.getWidth() - tw) / 2;
            int cy = sw.getY() + (sw.getHeight() - th) / 2 + 1;
            ctx.drawText(this.textRenderer, hash, cx, cy, col, false);
        }

        ctx.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double vertical) {
        int listTop = PADDING;
        int listBottom = this.height - PADDING;
        int listLeft = PADDING;
        int listRight = SIDEBAR_WIDTH - PADDING;

        boolean inList = mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom;

        if (inList) {
            int listH = Math.max(0, listBottom - listTop);
            int maxScroll = Math.max(0, contentHeight - listH);
            if (maxScroll > 0) {
                scrollY = (int) Math.max(0, Math.min(maxScroll, scrollY - vertical * 20));
            }
            return true;
        }

        if (isInBrowser(mouseX, mouseY)) {
            browser.sendMouseWheel(mouseXForBrowser(mouseX), mouseYForBrowser(mouseY), vertical, 0);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, vertical);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInBrowser(mouseX, mouseY)) {
            browser.sendMousePress(mouseXForBrowser(mouseX), mouseYForBrowser(mouseY), button);
            browser.setFocus(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isInBrowser(mouseX, mouseY)) {
            browser.sendMouseRelease(mouseXForBrowser(mouseX), mouseYForBrowser(mouseY), button);
            browser.setFocus(true);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isInBrowser(mouseX, mouseY)) {
            browser.sendMouseMove(mouseXForBrowser(mouseX), mouseYForBrowser(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        double mx = this.client.mouse.getX() / this.client.getWindow().getScaleFactor();
        double my = this.client.mouse.getY() / this.client.getWindow().getScaleFactor();
        if (isInBrowser(mx, my)) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            browser.setFocus(true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        double mx = this.client.mouse.getX() / this.client.getWindow().getScaleFactor();
        double my = this.client.mouse.getY() / this.client.getWindow().getScaleFactor();
        if (isInBrowser(mx, my)) {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
            browser.setFocus(true);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == (char) 0) return false;
        double mx = this.client.mouse.getX() / this.client.getWindow().getScaleFactor();
        double my = this.client.mouse.getY() / this.client.getWindow().getScaleFactor();
        if (isInBrowser(mx, my)) {
            browser.sendKeyTyped(codePoint, modifiers);
            browser.setFocus(true);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private String buildInitialUrl() {
        List<String> params = new ArrayList<>();

        for (Map.Entry<Harvestable, CheckboxWidget> e : itemChecks.entrySet()) {
            Harvestable rep = e.getKey();
            String id = rep.getName() != null ? rep.getName() : "unknown";

            String cat = repToCategory.get(rep);
            boolean catChecked = true;
            CheckboxWidget catCb = (cat != null) ? catChecks.get(cat) : null;
            if (catCb != null) catChecked = catCb.isChecked();

            boolean itemChecked = e.getValue().isChecked();
            boolean visible = catChecked && itemChecked;

            if (!visible) {
                params.add(id + "=false");
            }
        }

        String query = params.isEmpty() ? "" : "?" + String.join("&", params);
        return BASE_URL + query + MAP_HASH;
    }

    private void applyCategoryVisibilityToItems(String cat, boolean catChecked) {
        List<Harvestable> reps = byCategory.getOrDefault(cat, List.of());
        for (Harvestable rep : reps) {
            CheckboxWidget itCb = itemChecks.get(rep);
            if (itCb == null) continue;

            boolean itemChecked = itCb.isChecked();
            boolean shouldShow = catChecked && itemChecked;

            String id = rep.getName() != null ? rep.getName() : "unknown";
            js("if (window.mbxSetItem) { window.mbxSetItem(" + jsString(id) + ", " + shouldShow + "); }");
        }
    }

    private int browserLeft() {
        return SIDEBAR_WIDTH + PADDING;
    }
    private int browserTop() {
        return PADDING;
    }
    private int browserWidth() {
        return Math.max(0, this.width - (SIDEBAR_WIDTH + 2 * PADDING));
    }
    private int browserHeight() {
        return Math.max(0, this.height - 2 * PADDING);
    }

    private boolean isInBrowser(double x, double y) {
        return x >= browserLeft() && y >= browserTop()
                && x < browserLeft() + browserWidth()
                && y < browserTop() + browserHeight();
    }

    private int mouseXForBrowser(double x) {
        return (int) ((x - browserLeft()) * mc.getWindow().getScaleFactor());
    }

    private int mouseYForBrowser(double y) {
        return (int) ((y - browserTop()) * mc.getWindow().getScaleFactor());
    }

    private void resizeBrowser() {
        if (browser != null) {
            int scaledW = (int) (browserWidth() * mc.getWindow().getScaleFactor());
            int scaledH = (int) (browserHeight() * mc.getWindow().getScaleFactor());
            if (scaledW > 0 && scaledH > 0) {
                browser.resize(scaledW, scaledH);
            }
        }
    }

    private void updateFrame() {
        if (browser == null) return;
        browserTexture.setId(browser.getRenderer().getTextureID());
        browserTexture.setWidth(this.width);
        browserTexture.setHeight(this.height);
    }

    private List<String> sortedCategories() {
        ArrayList<String> keys = new ArrayList<>(byCategory.keySet());
        Collections.sort(keys);
        return keys;
    }

    private int savedColorOrCurrent(Harvestable rep) {
        return currentColors.getOrDefault(rep, 0xFFFFFFFF);
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
                cache.putIfAbsent(vid, Identifier.of("minecraft", "textures/item/" + vid + ".png"));
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

    private void js(String code) {
        if (browser != null) {
            browser.executeJavaScript(code, browser.getURL(), 0);
        }
    }

    private static String jsString(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static class ColorPickerScreen extends Screen {
        private final int initialColor;
        private final IntConsumer onPicked;
        private final Screen parent;

        private TextFieldWidget hexField;
        private int color;

        private final List<ButtonWidget> presetButtons = new ArrayList<>();
        private final Map<ButtonWidget, Integer> presetColors = new HashMap<>();

        protected ColorPickerScreen(int initialColor, IntConsumer onPicked, Screen parent) {
            super(Text.literal("Pick Color"));
            this.initialColor = initialColor & 0xFFFFFF;
            this.color = this.initialColor;
            this.onPicked = onPicked;
            this.parent = parent;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int y = 40;

            hexField = new TextFieldWidget(this.textRenderer, cx - 60, y, 120, 20, Text.literal("Hex"));
            hexField.setMaxLength(7);
            hexField.setText("#" + toHex(color));
            hexField.setChangedListener(this::onHexChanged);
            this.addDrawableChild(hexField);
            y += 26;

            int[] presets = new int[] {
                    0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF,
                    0xFFFF00, 0x00FFFF, 0xFF00FF, 0xFFA500
            };
            int startX = cx - (presets.length * 22) / 2;
            for (int i = 0; i < presets.length; i++) {
                final int c = presets[i];
                ButtonWidget presetBtn = ButtonWidget.builder(Text.literal(""), b -> {
                            color = c;
                            hexField.setText("#" + toHex(color));
                        })
                        .dimensions(startX + i * 22, y, 18, 18)
                        .build();
                this.addDrawableChild(presetBtn);
                presetButtons.add(presetBtn);
                presetColors.put(presetBtn, c);
            }
            y += 28;

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
                onPicked.accept(color);
                this.close();
            }).dimensions(cx - 80, y, 70, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
                this.client.setScreen(parent);
            }).dimensions(cx + 10, y, 70, 20).build());
        }

        private void onHexChanged(String text) {
            int parsed = parseHexPartial(text, color);
            if (parsed != -1) {
                color = parsed;
            }
        }

        @Override
        public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
            int cx = this.width / 2;

            draw.drawText(this.textRenderer, "Pick Beam Color", cx - 50, 16, 0xFFFFFF, false);

            int px0 = cx - 40, py0 = 20, px1 = cx + 40, py1 = 36;
            int col = 0xFF000000 | color;
            draw.fill(px0 - 1, py0 - 1, px1 + 1, py1 + 1, 0xFF000000);
            draw.fill(px0, py0, px1, py1, col);

            super.render(draw, mouseX, mouseY, delta);

            for (ButtonWidget btn : presetButtons) {
                int c = presetColors.getOrDefault(btn, 0xFFFFFFFF) | 0xFF000000;
                String hash = "#";
                int tw = this.textRenderer.getWidth(hash);
                int th = this.textRenderer.fontHeight;
                int cxTxt = btn.getX() + (btn.getWidth() - tw) / 2;
                int cyTxt = btn.getY() + (btn.getHeight() - th) / 2 + 1;
                draw.drawText(this.textRenderer, hash, cxTxt, cyTxt, c, false);
            }
        }

        @Override
        public void close() {
            this.client.setScreen(parent);
        }

        private static String toHex(int rgb) {
            String s = Integer.toHexString(rgb & 0xFFFFFF).toUpperCase(Locale.ROOT);
            while (s.length() < 6) s = "0" + s;
            return s;
        }

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
}