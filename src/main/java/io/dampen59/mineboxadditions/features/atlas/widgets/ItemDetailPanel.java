package io.dampen59.mineboxadditions.features.atlas.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.bestiary.BestiaryEntry;
import io.dampen59.mineboxadditions.features.bestiary.BestiaryListWidget;
import io.dampen59.mineboxadditions.features.bestiary.BestiaryScreen;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.Supplier;

public class ItemDetailPanel implements Renderable, GuiEventListener, NarratableEntry {

    private final Supplier<MineboxItem> itemSupplier;
    private final int x, y, width, height;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentHeight = 0;
    private static final int SCROLL_STEP = 12;

    private boolean isLocked = false;
    private MineboxItem lockedItem = null;
    private Integer pendingScrollOffset = null;

    private Button lockButton;
    private EditBox quantityField;
    private Button clipboardButton;
    private int quantity = 1;

    private final Set<String> collapsed = new HashSet<>();
    private final Map<String, ClickRegion> toggleRegions = new HashMap<>();

    private static final int UI_PAD    = 6;
    private static final int CTRL_H    = 14;
    private static final int HEADER_H  = CTRL_H + UI_PAD * 2;
    private static final int PILL_H    = 11;
    private static final int PILL_PAD  = 4;
    private static final int SECTION_W = 2;

    private final Map<String, List<MineboxItem>> usedInCache = new HashMap<>();
    private java.util.function.Consumer<MineboxItem> onNavigate;
    private final List<ClickRegionUsedIn> usedInRegions = new ArrayList<>();

    private static final class ClickRegion {
        final int x, y, w, h;
        ClickRegion(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean contains(double mx, double my) { return mx >= x && mx <= x+w && my >= y && my <= y+h; }
    }

    private static final class ClickRegionUsedIn {
        final ClickRegion region;
        final MineboxItem target;
        ClickRegionUsedIn(ClickRegion r, MineboxItem t) { region = r; target = t; }
    }

    private static final class ClickRegionBestiary {
        final ClickRegion region;
        final String bestiaryId;
        ClickRegionBestiary(ClickRegion r, String id) { region = r; bestiaryId = id; }
    }
    private final List<ClickRegionBestiary> droppedByRegions = new ArrayList<>();

    private String lastPreloadedItemId = null;

    public ItemDetailPanel(Supplier<MineboxItem> itemSupplier, int x, int y, int width, int height) {
        this.itemSupplier = itemSupplier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setOnNavigate(java.util.function.Consumer<MineboxItem> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void initLockButton(Screen parent) {
        int rowY  = y + (HEADER_H - CTRL_H) / 2;
        Font font = Minecraft.getInstance().font;
        int fieldW = Math.max(36, font.width("0000") + 10);
        int btnW = 16;

        quantityField = new EditBox(font, x + UI_PAD, rowY, fieldW, CTRL_H, Component.literal("Qty"));
        quantityField.setMaxLength(4);
        quantityField.setValue(String.valueOf(quantity));
        quantityField.setResponder(text -> {
            try {
                int q = Integer.parseInt(text.trim());
                if (q > 0) {
                    quantity = q;
                    if (isLocked && lockedItem != null)
                        MineboxAdditions.INSTANCE.state.setLockedItemQuantity(quantity);
                }
            } catch (NumberFormatException ignored) {}
        });
        parent.addRenderableWidget(quantityField);

        lockButton = Button.builder(Component.literal(isLocked ? "🔒" : "🔓"), btn -> {
            isLocked = !isLocked;
            lockButton.setMessage(Component.literal(isLocked ? "🔒" : "🔓"));
            if (isLocked) {
                lockedItem = itemSupplier.get();
                if (lockedItem != null) {
                    MineboxAdditions.INSTANCE.state.setLockedItemId(lockedItem.getId());
                    MineboxAdditions.INSTANCE.state.setLockedItemScrollOffset(scrollOffset);
                    MineboxAdditions.INSTANCE.state.setLockedItemQuantity(quantity);
                }
            } else {
                unlock();
            }
        }).bounds(x + UI_PAD + fieldW + UI_PAD, rowY, btnW, CTRL_H).build();
        parent.addRenderableWidget(lockButton);

        clipboardButton = Button.builder(Component.literal("📋"), btn -> {
            MineboxItem item = isLocked ? lockedItem : itemSupplier.get();
            if (item == null || item.getRecipe() == null || item.getRecipe().getIngredients() == null) return;
            Map<String, Integer> flat = new HashMap<>();
            for (MineboxItem.Ingredient ing : item.getRecipe().getIngredients())
                collectBaseIngredients(ing, quantity, flat);
            List<String> lines = new ArrayList<>();
            for (var e : flat.entrySet()) {
                String[] parts = e.getKey().split(":", 2);
                boolean vanilla = "v".equals(parts[0]);
                String id = parts[1];
                String name;
                if (vanilla) {
                    Item mcItem = BuiltInRegistries.ITEM.getOptional(
                            Identifier.fromNamespaceAndPath("minecraft", id)).orElse(null);
                    name = mcItem != null ? Component.translatable(mcItem.getDescriptionId()).getString() : id;
                } else {
                    name = MineboxItem.getDisplayName(MineboxAdditions.INSTANCE.state.getItemById(id)).getString();
                }
                lines.add("- " + e.getValue() + "x " + name);
            }
            lines.sort(String::compareToIgnoreCase);
            Minecraft.getInstance().keyboardHandler.setClipboard(String.join("\n", lines));
            Utils.showToastNotification(
                    Component.translatable("mineboxadditions.gui.atlas.clipboard.title").getString(),
                    Component.translatable("mineboxadditions.gui.atlas.clipboard.desc").getString());
        }).bounds(x + UI_PAD + fieldW + UI_PAD + btnW + UI_PAD, rowY, btnW, CTRL_H).build();
        parent.addRenderableWidget(clipboardButton);
    }

    private void collectBaseIngredients(MineboxItem.Ingredient ing, int multiplier, Map<String, Integer> out) {
        int amount = ing.getAmount() * multiplier;
        boolean hasSub = !ing.isVanilla()
                && ing.getCustomItem() != null
                && ing.getCustomItem().getRecipe() != null
                && ing.getCustomItem().getRecipe().getIngredients() != null
                && !ing.getCustomItem().getRecipe().getIngredients().isEmpty();
        if (hasSub)
            for (MineboxItem.Ingredient sub : ing.getCustomItem().getRecipe().getIngredients())
                collectBaseIngredients(sub, amount, out);
        else
            out.merge((ing.isVanilla() ? "v:" : "c:") + ing.getId(), amount, Integer::sum);
    }

    public void unlock() {
        isLocked = false;
        lockedItem = null;
        quantity = 1;
        if (quantityField != null) quantityField.setValue("1");
        if (lockButton != null) lockButton.setMessage(Component.literal("🔓"));
        MineboxAdditions.INSTANCE.state.setLockedItemId(null);
        MineboxAdditions.INSTANCE.state.setLockedItemScrollOffset(null);
        MineboxAdditions.INSTANCE.state.setLockedCollapsedKeys(Collections.emptySet());
    }

    public void lock(MineboxItem item) {
        isLocked = true;
        lockedItem = item;
        if (lockButton != null) lockButton.setMessage(Component.literal("🔒"));
        int savedQty = MineboxAdditions.INSTANCE.state.getLockedItemQuantity();
        quantity = savedQty > 0 ? savedQty : 1;
        if (quantityField != null) quantityField.setValue(String.valueOf(quantity));
        MineboxAdditions.INSTANCE.state.setLockedItemQuantity(quantity);
        Integer savedScroll = MineboxAdditions.INSTANCE.state.getLockedItemScrollOffset();
        if (savedScroll != null) pendingScrollOffset = savedScroll;
        collapsed.clear();
        collapsed.addAll(MineboxAdditions.INSTANCE.state.getLockedCollapsedKeys());
    }

    // ─── Render ──────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        toggleRegions.clear();
        usedInRegions.clear();
        droppedByRegions.clear();

        MineboxItem item = isLocked ? lockedItem : itemSupplier.get();
        if (item == null) return;
        if (!item.getId().equals(lastPreloadedItemId)) preloadUsedInTextures(item);
        if (pendingScrollOffset != null) scrollOffset = Math.max(0, pendingScrollOffset);

        Font font = Minecraft.getInstance().font;
        final int lh = font.lineHeight;
        final int lm = x + UI_PAD;

        ctx.pose().pushMatrix();

        ctx.fill(x,     y,          x + width,     y + height,    0xAA080808);
        ctx.fill(x,     y,          x + width,     y + 1,         0x44FFFFFF);
        ctx.fill(x,     y + height - 1, x + width, y + height,    0x44FFFFFF);
        ctx.fill(x,     y,          x + 1,         y + height,    0x44FFFFFF);
        ctx.fill(x + width - 1, y,  x + width,     y + height,    0x44FFFFFF);

        ctx.fill(x, y, x + width, y + HEADER_H, 0x99111111);
        ctx.fill(x, y + HEADER_H - 1, x + width, y + HEADER_H, 0x33FFFFFF);

        int rowY   = y + (HEADER_H - CTRL_H) / 2;
        Font tr    = font;
        int fieldW = Math.max(36, tr.width("0000") + 10);
        int btnW   = 16;
        int sp     = UI_PAD;
        String qtyLabel = "Qty:";
        int qtyLW = tr.width(qtyLabel);
        int totalW = qtyLW + sp + fieldW + sp + btnW + sp + btnW;
        int csx    = x + (width - totalW) / 2;

        ctx.text(tr, Component.literal(qtyLabel), csx, rowY + (CTRL_H - lh) / 2, 0xFF888888, false);
        if (quantityField  != null) { quantityField.setX(csx + qtyLW + sp); quantityField.setY(rowY); quantityField.setWidth(fieldW); }
        if (lockButton     != null) { lockButton.setX(csx + qtyLW + sp + fieldW + sp); lockButton.setY(rowY); }
        if (clipboardButton!= null) { clipboardButton.setX(csx + qtyLW + sp + fieldW + sp + btnW + sp); clipboardButton.setY(rowY); }

        ctx.enableScissor(x + 1, y + HEADER_H, x + width - 1, y + height - 1);
        ctx.pose().translate(0f, (float) -scrollOffset);

        int drawY = y + HEADER_H + 10;

        final int heroSz = 48;
        final int iconSz = 32;
        int heroX = x + (width - heroSz) / 2;
        ctx.fill(heroX - 2, drawY - 2, heroX + heroSz + 2, drawY + heroSz + 2, 0x22FFFFFF);
        ctx.fill(heroX - 1, drawY - 1, heroX + heroSz + 1, drawY + heroSz + 1, 0x33FFFFFF);
        ctx.fill(heroX,     drawY,     heroX + heroSz,     drawY + heroSz,      0x11FFFFFF);
        Identifier icon = ItemListWidget.ItemEntry.getTexture(item.getId());
        if (icon != null) {
            int ix = heroX + (heroSz - iconSz) / 2;
            int iy = drawY + (heroSz - iconSz) / 2;
            ctx.blit(RenderPipelines.GUI_TEXTURED, icon, ix, iy, 0, 0, iconSz, iconSz, iconSz, iconSz);
        }
        drawY += heroSz + 8;

        Component nameText = MineboxItem.getDisplayName(item);
        int nameColor = 0xFFFFFFFF;
        if (item.getRarity() != null)
            nameColor = RaritiesUtils.getRarityColor(item.getRarity().toLowerCase()).getRGB() | 0xFF000000;
        int nameW = font.width(nameText);
        ctx.text(font, nameText, x + (width - nameW) / 2, drawY, nameColor, true);
        drawY += lh + 3;

        String lore = MineboxItem.getLoreText(item.getId());
        if (!lore.isEmpty()) {
            String fullLore = "« " + lore + " »";
            int loreW = font.width(fullLore);
            ctx.text(font, Component.literal(fullLore), x + (width - loreW) / 2, drawY, 0xFF666666, false);
            drawY += lh + 2;
        }
        drawY += 6;

        String lvlStr  = "Lvl " + item.getLevel();
        String catStr  = item.getCategory() != null ? item.getCategory() : "—";
        String rarStr  = item.getRarity()   != null ? capitalize(item.getRarity()) : "—";
        int wLvl  = font.width(lvlStr)  + PILL_PAD * 2;
        int wCat  = font.width(catStr)  + PILL_PAD * 2;
        int wRar  = font.width(rarStr)  + PILL_PAD * 2;
        int pillsW = wLvl + 4 + wCat + 4 + wRar;
        int px    = x + (width - pillsW) / 2;
        int py    = drawY;
        int pillTextY = py + (PILL_H - lh) / 2;

        ctx.fill(px, py, px + wLvl, py + PILL_H, 0x44FFFFFF);
        ctx.text(font, Component.literal(lvlStr), px + PILL_PAD, pillTextY, 0xFFBBBBBB, false);

        px += wLvl + 4;
        ctx.fill(px, py, px + wCat, py + PILL_H, 0x44FFFFFF);
        ctx.text(font, Component.literal(catStr), px + PILL_PAD, pillTextY, 0xFFBBBBBB, false);

        px += wCat + 4;
        int rarBg = item.getRarity() != null
                ? (RaritiesUtils.getRarityColor(item.getRarity().toLowerCase()).getRGB() & 0x00FFFFFF) | 0x55000000
                : 0x44FFFFFF;
        ctx.fill(px, py, px + wRar, py + PILL_H, rarBg);
        ctx.text(font, Component.literal(rarStr), px + PILL_PAD, pillTextY, nameColor, false);

        drawY += PILL_H + 10;

        if (item.getMbxStats() != null && !item.getMbxStats().isEmpty()) {
            drawY = sectionHeader(ctx, font, Component.translatable("mineboxadditions.gui.atlas.stats").getString(), lm, drawY, width - UI_PAD * 2);
            for (var entry : item.getMbxStats().entrySet()) {
                var statVal = entry.getValue();
                Component line = Component.literal("  ")
                        .append(MineboxItem.getStatName(entry.getKey()))
                        .append(Component.literal(": " + statVal.getMin() + " – " + statVal.getMax())
                                .withStyle(s -> s.withColor(0xFFDDDDDD)));
                ctx.text(font, line, lm + 6, drawY, 0xFFAAAAAA, false);
                drawY += lh + 2;
            }
            drawY += 6;
        }

        if (item.getRecipe() != null && item.getRecipe().getIngredients() != null) {
            drawY = sectionHeader(ctx, font, Component.translatable("mineboxadditions.gui.atlas.recipe").getString(), lm, drawY, width - UI_PAD * 2);
            List<MineboxItem.Ingredient> ings = item.getRecipe().getIngredients();
            for (int i = 0; i < ings.size(); i++)
                drawY = renderIngredient(ctx, font, ings.get(i), lm, drawY, 0, quantity, i == ings.size() - 1, "root");
            drawY += 6;
        }

        List<MineboxItem> usedIn = usedInCache.computeIfAbsent(item.getId(), key ->
                MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                        .filter(o -> o.getRecipe() != null && o.getRecipe().getIngredients() != null)
                        .filter(o -> o.getRecipe().getIngredients().stream()
                                .anyMatch(ing -> !ing.isVanilla() && item.getId().equals(ing.getId())))
                        .toList()
        );
        if (!usedIn.isEmpty()) {
            drawY = sectionHeader(ctx, font, Component.translatable("mineboxadditions.gui.atlas.used_in").getString(), lm, drawY, width - UI_PAD * 2);
            for (MineboxItem parent : usedIn) {
                Identifier pIcon = ItemListWidget.ItemEntry.getTexture(parent.getId());
                if (pIcon != null)
                    ctx.blit(RenderPipelines.GUI_TEXTURED, pIcon, lm + 6, drawY, 0, 0, 16, 16, 16, 16);
                Component pName = MineboxItem.getDisplayName(parent);
                ctx.text(font, pName, lm + 26, drawY + (16 - lh) / 2, 0xFFDDDDDD, false);
                usedInRegions.add(new ClickRegionUsedIn(
                        new ClickRegion(lm + 6, drawY, 20 + font.width(pName), 16), parent));
                drawY += 20;
            }
            drawY += 4;
        }

        List<BestiaryEntry> bestiary = MineboxAdditions.INSTANCE.state.getMbxBestiary();
        if (bestiary != null) {
            List<BestiaryEntry> droppedBy = bestiary.stream()
                    .filter(e -> e.getDrops() != null && e.getDrops().stream()
                            .anyMatch(d -> item.getId().equals(d.getItemId())))
                    .toList();
            if (!droppedBy.isEmpty()) {
                drawY = sectionHeader(ctx, font, Component.translatable("mineboxadditions.gui.atlas.dropped_by").getString(), lm, drawY, width - UI_PAD * 2);
                for (BestiaryEntry entry : droppedBy) {
                    Identifier bIcon = BestiaryListWidget.EntryRow.loadAndCacheTexture(entry);
                    if (bIcon != null) {
                        ctx.pose().pushMatrix();
                        ctx.pose().translate((float)(lm + 6), (float) drawY);
                        ctx.pose().scale(16f / 256, 16f / 256);
                        ctx.blit(RenderPipelines.GUI_TEXTURED, bIcon, 0, 0, 0, 0, 256, 256, 256, 256);
                        ctx.pose().popMatrix();
                    }
                    Component eName = Component.literal(entry.getName());
                    ctx.text(font, eName, lm + 26, drawY + (16 - lh) / 2, 0xFFDDDDDD, false);
                    droppedByRegions.add(new ClickRegionBestiary(
                            new ClickRegion(lm + 6, drawY, 20 + font.width(eName), 16), entry.getId()));
                    drawY += 20;
                }
                drawY += 4;
            }
        }

        contentHeight = drawY - (y + HEADER_H);
        maxScroll = Math.max(0, contentHeight - (height - HEADER_H));

        if (pendingScrollOffset != null) {
            scrollOffset = Math.max(0, Math.min(pendingScrollOffset, maxScroll));
            pendingScrollOffset = null;
        }

        ctx.disableScissor();
        ctx.pose().popMatrix();
    }

    private int sectionHeader(GuiGraphicsExtractor ctx, Font font, String label, int x, int y, int w) {
        int lineY = y + font.lineHeight / 2;
        ctx.fill(x, lineY, x + w, lineY + 1, 0x22FFFFFF);
        int labelW = font.width(label);
        int lx = x + SECTION_W + 5;
        ctx.fill(lx - 2, y - 1, lx + labelW + 2, y + font.lineHeight + 1, 0xCC080808);
        ctx.fill(x, y - 1, x + SECTION_W, y + font.lineHeight + 1, 0xFFBBBBBB);
        ctx.text(font, Component.literal(label), lx, y, 0xFFBBBBBB, false);
        return y + font.lineHeight + 7;
    }

    private int renderIngredient(GuiGraphicsExtractor ctx, Font font,
                                 MineboxItem.Ingredient ing, int x, int y, int depth,
                                 int multiplier, boolean isLast, String path) {
        final int iconSz  = 16;
        final int spacing = 5;
        final int indent  = (1 + depth) * 12;
        final int iconX   = x + indent;

        int amount = ing.getAmount() * multiplier;
        boolean hasItem = playerHasIngredient(ing, multiplier);
        int count       = playerIngredientCount(ing);
        int stacks      = amount / 64;
        int rem         = amount % 64;

        boolean expandable = !ing.isVanilla()
                && ing.getCustomItem() != null
                && ing.getCustomItem().getRecipe() != null
                && ing.getCustomItem().getRecipe().getIngredients() != null
                && !ing.getCustomItem().getRecipe().getIngredients().isEmpty();

        String key = nodeKey(path, ing);
        if (expandable) {
            boolean collapsed_ = !collapsed.contains(key);
            ctx.text(font, Component.literal(collapsed_ ? "▶" : "▼"),
                    iconX - 10, y + (iconSz - font.lineHeight) / 2, 0xFF777777, false);
            toggleRegions.put(key, new ClickRegion(iconX - 12, y, 12, iconSz));
        }

        if (ing.isVanilla()) {
            ItemStack stack = ing.getVanillaStack();
            if (!stack.isEmpty()) ctx.item(stack, iconX, y);
        } else {
            Identifier tex = ing.getTexture();
            if (tex != null)
                ctx.blit(RenderPipelines.GUI_TEXTURED, tex, iconX, y, 0, 0, iconSz, iconSz, iconSz, iconSz);
        }

        int tx = iconX + iconSz + 4;
        int ty = y + (iconSz - font.lineHeight) / 2;

        MutableComponent line = Component.literal(String.valueOf(amount));
        if (stacks > 0) {
            String si = rem > 0 ? String.format(" (%ds+%d)", stacks, rem) : String.format(" (%ds)", stacks);
            line.append(Component.literal(si).withStyle(s -> s.withColor(0xFFAF8E26)));
        }
        line.append(Component.literal(" × ")).append(ingredientName(ing));
        line.append(hasItem
                ? Component.literal(" ✅").withStyle(s -> s.withColor(0xFF55FF55))
                : Component.literal(" ❌ (" + count + "/" + amount + ")").withStyle(s -> s.withColor(0xFFFF5555)));

        ctx.text(font, line, tx, ty, 0xFFEEEEEE, false);
        y += iconSz + spacing;

        if (expandable && collapsed.contains(key)) {
            var subs = ing.getCustomItem().getRecipe().getIngredients();
            for (int i = 0; i < subs.size(); i++) {
                y = renderIngredient(ctx, font, subs.get(i),
                        x, y, depth + 1, amount, i == subs.size() - 1, path + ">" + ing.getId() + "#" + i);
            }
        }
        return y;
    }

    private static Component ingredientName(MineboxItem.Ingredient ing) {
        if (ing.isVanilla()) {
            Item mcItem = BuiltInRegistries.ITEM.getOptional(
                    Identifier.fromNamespaceAndPath("minecraft", ing.getId())).orElse(null);
            return mcItem != null ? Component.translatable(mcItem.getDescriptionId()) : Component.literal(ing.getId());
        }
        MineboxItem sub = ing.getCustomItem();
        return sub != null ? MineboxItem.getDisplayName(sub) : Component.literal(ing.getId());
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private boolean playerHasIngredient(MineboxItem.Ingredient ing, int multiplier) {
        if (Minecraft.getInstance().player == null) return false;
        var inv = Minecraft.getInstance().player.getInventory();
        int required = ing.getAmount() * multiplier;
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (Utils.isMineboxItem(stack)) {
                String id = Utils.processIdMismatch(Utils.getMineboxItemId(stack));
                if (id != null && id.equals(ing.getId())) {
                    found += stack.getCount();
                    if (found >= required) return true;
                }
            } else if (ing.isVanilla()) {
                Item it = BuiltInRegistries.ITEM.getOptional(
                        Identifier.fromNamespaceAndPath("minecraft", ing.getId())).orElse(null);
                if (stack.getItem() == it) {
                    found += stack.getCount();
                    if (found >= required) return true;
                }
            }
        }
        return false;
    }

    private int playerIngredientCount(MineboxItem.Ingredient ing) {
        if (Minecraft.getInstance().player == null) return -1;
        var inv = Minecraft.getInstance().player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (Utils.isMineboxItem(stack)) {
                String id = Utils.processIdMismatch(Utils.getMineboxItemId(stack));
                if (id != null && id.equals(ing.getId())) total += stack.getCount();
            } else if (ing.isVanilla()) {
                Item it = BuiltInRegistries.ITEM.getOptional(
                        Identifier.fromNamespaceAndPath("minecraft", ing.getId())).orElse(null);
                if (stack.getItem() == it) total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (mx < x || mx > x + width || my < y + HEADER_H || my > y + height) return false;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int)(v * SCROLL_STEP), maxScroll));
        if (isLocked) MineboxAdditions.INSTANCE.state.setLockedItemScrollOffset(scrollOffset);
        return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (mx < x || mx > x + width || my < y || my > y + height) return false;
        double adjY = my + scrollOffset;

        for (var e : toggleRegions.entrySet()) {
            if (e.getValue().contains(mx, adjY)) {
                String key = e.getKey();
                boolean wasExpanded = collapsed.contains(key);
                if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT))
                    toggleSubtree(key, wasExpanded);
                else if (wasExpanded) collapsed.remove(key);
                else collapsed.add(key);
                if (isLocked) MineboxAdditions.INSTANCE.state.setLockedCollapsedKeys(new HashSet<>(collapsed));
                return true;
            }
        }
        for (var ui : usedInRegions) {
            if (ui.region.contains(mx, adjY)) {
                preloadTextures(ui.target);
                if (onNavigate != null) onNavigate.accept(ui.target);
                pendingScrollOffset = 0;
                return true;
            }
        }
        for (var b : droppedByRegions) {
            if (b.region.contains(mx, adjY)) {
                Minecraft.getInstance().gui.setScreen(new BestiaryScreen(b.bestiaryId));
                return true;
            }
        }
        return false;
    }

    private void toggleSubtree(String rootKey, boolean expand) {
        if (expand) {
            collapsed.remove(rootKey);
            collapsed.removeIf(k -> k.startsWith(rootKey + ">"));
        } else {
            collapsed.add(rootKey);
        }
    }

    private String nodeKey(String path, MineboxItem.Ingredient ing) {
        return path + ">" + (ing.isVanilla() ? "v:" : "c:") + ing.getId();
    }

    @Override public void setFocused(boolean focused) { }
    @Override public boolean isFocused() { return false; }
    @Override public void updateNarration(NarrationElementOutput b) { }
    @Override public NarratableEntry.NarrationPriority narrationPriority() { return NarratableEntry.NarrationPriority.HOVERED; }

    public void setControlsVisible(boolean visible) {
        if (quantityField   != null) quantityField.setVisible(visible);
        if (lockButton      != null) lockButton.visible = visible;
        if (clipboardButton != null) clipboardButton.visible = visible;
    }

    private void preloadTextures(MineboxItem item) {
        if (item == null) return;
        ItemListWidget.ItemEntry.getTextureCache()
                .computeIfAbsent(item.getId(), id -> ItemListWidget.ItemEntry.loadTexture(item.getId(), item.getTexture()));
        if (item.getRecipe() != null && item.getRecipe().getIngredients() != null)
            for (var ing : item.getRecipe().getIngredients()) preloadIngredientTextures(ing);
    }

    private void preloadIngredientTextures(MineboxItem.Ingredient ing) {
        if (ing.isVanilla()) {
            ItemListWidget.ItemEntry.getTextureCache().putIfAbsent(ing.getId(),
                    Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + ing.getId() + ".png"));
            return;
        }
        MineboxItem sub = ing.getCustomItem();
        if (sub != null) {
            ItemListWidget.ItemEntry.getTextureCache()
                    .computeIfAbsent(sub.getId(), id -> ItemListWidget.ItemEntry.loadTexture(sub.getId(), sub.getTexture()));
            if (sub.getRecipe() != null && sub.getRecipe().getIngredients() != null)
                for (var subIng : sub.getRecipe().getIngredients()) preloadIngredientTextures(subIng);
        }
    }

    private void preloadUsedInTextures(MineboxItem item) {
        if (item == null || item.getId().equals(lastPreloadedItemId)) return;
        lastPreloadedItemId = item.getId();
        List<MineboxItem> usedIn = usedInCache.computeIfAbsent(item.getId(), key ->
                MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                        .filter(o -> o.getRecipe() != null && o.getRecipe().getIngredients() != null)
                        .filter(o -> o.getRecipe().getIngredients().stream()
                                .anyMatch(ing -> !ing.isVanilla() && item.getId().equals(ing.getId())))
                        .toList()
        );
        for (MineboxItem parent : usedIn) {
            ItemListWidget.ItemEntry.getTextureCache()
                    .computeIfAbsent(parent.getId(), id -> ItemListWidget.ItemEntry.loadTexture(parent.getId(), parent.getTexture()));
            if (parent.getRecipe() != null && parent.getRecipe().getIngredients() != null)
                for (var ing : parent.getRecipe().getIngredients()) preloadIngredientTextures(ing);
        }
    }
}
