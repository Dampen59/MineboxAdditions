package io.dampen59.mineboxadditions.features.atlas.widgets;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Supplier;

public class ItemDetailPanel implements Drawable, Element, Selectable {

    private final Supplier<MineboxItem> itemSupplier;
    private final int x, y, width, height;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int contentHeight = 0;
    private static final int SCROLL_STEP = 12;
    private boolean isLocked = false;
    private MineboxItem lockedItem = null;
    private Integer pendingScrollOffset = null;
    private ButtonWidget lockButton;
    private TextFieldWidget quantityField;
    private ButtonWidget clipboardButton;
    private int quantity = 1;
    private final Set<String> collapsed = new HashSet<>();
    private final Map<String, ClickRegion> toggleRegions = new HashMap<>();

    private static final int UI_PADDING = 6;
    private static final int CTRL_H = 14;
    private static final int HEADER_H = CTRL_H + UI_PADDING * 2;
    private final Map<String, List<MineboxItem>> usedInCache = new HashMap<>();

    private java.util.function.Consumer<MineboxItem> onNavigate;
    private final List<ClickRegionUsedIn> usedInRegions = new ArrayList<>();

    private String lastPreloadedItemId = null;

    public ItemDetailPanel(Supplier<MineboxItem> itemSupplier, int x, int y, int width, int height) {
        this.itemSupplier = itemSupplier;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

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

    public void setOnNavigate(java.util.function.Consumer<MineboxItem> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void initLockButton(Screen parent) {
        int btnW = 16;
        int btnH = 16;
        int rowY = y + UI_PADDING + (HEADER_H - CTRL_H) / 2;

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int fieldW = Math.max(36, tr.getWidth("0000") + 10);

        quantityField = new TextFieldWidget(
                tr,
                x + UI_PADDING, rowY, fieldW, CTRL_H,
                Text.of("Qty"));
        quantityField.setMaxLength(4);
        quantityField.setText(String.valueOf(quantity));
        quantityField.setChangedListener(text -> {
            try {
                int newQty = Integer.parseInt(text.trim());
                if (newQty > 0) {
                    quantity = newQty;
                    if (isLocked && lockedItem != null) {
                        MineboxAdditions.INSTANCE.state.setLockedItemQuantity(quantity);
                    }
                }
            } catch (NumberFormatException ignored) {}
        });
        parent.addDrawableChild(quantityField);

        int buttonX = x + width - btnW - UI_PADDING;
        int buttonY = rowY;
        lockButton = ButtonWidget.builder(Text.of(isLocked ? "🔒" : "🔓"), btn -> {
            isLocked = !isLocked;
            lockButton.setMessage(Text.of(isLocked ? "🔒" : "🔓"));
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
        }).dimensions(buttonX, buttonY, btnW, btnH).build();
        parent.addDrawableChild(lockButton);

        clipboardButton = ButtonWidget.builder(Text.of("📋"), btn -> {
            MineboxItem item = isLocked ? lockedItem : itemSupplier.get();
            if (item == null || item.getRecipe() == null || item.getRecipe().getIngredients() == null) return;
            Map<String, Integer> flat = new HashMap<>();
            for (MineboxItem.Ingredient ing : item.getRecipe().getIngredients()) {
                collectBaseIngredients(ing, quantity, flat);
            }
            List<String> lines = new ArrayList<>();
            for (var entry : flat.entrySet()) {
                String[] parts = entry.getKey().split(":", 2);
                boolean isVanilla = "v".equals(parts[0]);
                String id = parts[1];
                String name;
                if (isVanilla) {
                    Identifier rid = Identifier.of("minecraft", id);
                    Item mcItem = Registries.ITEM.get(rid);
                    name = Text.translatable(mcItem.getTranslationKey()).getString();
                } else {
                    name = MineboxItem.getDisplayName(MineboxAdditions.INSTANCE.state.getItemById(id)).getString();
                }
                lines.add("- " + entry.getValue() + "x " + name);
            }
            lines.sort(String::compareToIgnoreCase);
            MinecraftClient.getInstance().keyboard.setClipboard(String.join("\n", lines));
            Utils.showToastNotification("Copied to clipboard", "The recipe has been copied to your clipboard!");
        }).dimensions(x + UI_PADDING, rowY, 16, 16).build();
        parent.addDrawableChild(clipboardButton);
    }

    private void collectBaseIngredients(MineboxItem.Ingredient ing, int multiplier, Map<String, Integer> out) {
        int amount = ing.getAmount() * multiplier;

        boolean hasSubRecipe = !ing.isVanilla()
                && ing.getCustomItem() != null
                && ing.getCustomItem().getRecipe() != null
                && ing.getCustomItem().getRecipe().getIngredients() != null
                && !ing.getCustomItem().getRecipe().getIngredients().isEmpty();

        if (hasSubRecipe) {
            for (MineboxItem.Ingredient sub : ing.getCustomItem().getRecipe().getIngredients()) {
                collectBaseIngredients(sub, amount, out);
            }
        } else {
            String key = (ing.isVanilla() ? "v:" : "c:") + ing.getId();
            out.merge(key, amount, Integer::sum);
        }
    }

    public void unlock() {
        isLocked = false;
        lockedItem = null;
        quantity = 1;
        if (quantityField != null) quantityField.setText("1");
        if (lockButton != null) lockButton.setMessage(Text.of("🔓"));

        MineboxAdditions.INSTANCE.state.setLockedItemId(null);
        MineboxAdditions.INSTANCE.state.setLockedItemScrollOffset(null);

        MineboxAdditions.INSTANCE.state.setLockedCollapsedKeys(Collections.emptySet());
    }

    public void lock(MineboxItem item) {
        this.isLocked = true;
        this.lockedItem = item;

        if (lockButton != null) lockButton.setMessage(Text.of("🔒"));

        int savedQty = MineboxAdditions.INSTANCE.state.getLockedItemQuantity();
        quantity = savedQty > 0 ? savedQty : 1;
        if (quantityField != null) quantityField.setText(String.valueOf(quantity));
        MineboxAdditions.INSTANCE.state.setLockedItemQuantity(quantity);

        Integer savedScroll = MineboxAdditions.INSTANCE.state.getLockedItemScrollOffset();
        if (savedScroll != null) pendingScrollOffset = savedScroll;

        collapsed.clear();
        collapsed.addAll(MineboxAdditions.INSTANCE.state.getLockedCollapsedKeys());
    }

    private boolean playerHasIngredient(MineboxItem.Ingredient ingredient, int multiplier) {
        if (MinecraftClient.getInstance().player == null)
            return false;

        var inventory = MinecraftClient.getInstance().player.getInventory();
        int requiredAmount = ingredient.getAmount() * multiplier;
        int totalCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isEmpty())
                continue;

            if (Utils.isMineboxItem(stack)) {
                String mbxItemIdRaw = Utils.getMineboxItemId(stack);
                String mbxItemId = Utils.processIdMismatch(mbxItemIdRaw);
                if (mbxItemId != null && mbxItemId.equals(ingredient.getId())) {
                    totalCount += stack.getCount();
                    if (totalCount >= requiredAmount)
                        return true;
                }
            } else if (ingredient.isVanilla()) {
                Item ingredientItem = Registries.ITEM.get(Identifier.of("minecraft", ingredient.getId()));
                if (stack.getItem() == ingredientItem) {
                    totalCount += stack.getCount();
                    if (totalCount >= requiredAmount)
                        return true;
                }
            }

        }

        return false;
    }

    private int playerIngredientCount(MineboxItem.Ingredient ingredient) {
        if (MinecraftClient.getInstance().player == null)
            return -1;

        var inventory = MinecraftClient.getInstance().player.getInventory();
        int totalCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getStack(i);
            if (stack.isEmpty())
                continue;

            if (Utils.isMineboxItem(stack)) {
                String mbxItemIdRaw = Utils.getMineboxItemId(stack);
                String mbxItemId = Utils.processIdMismatch(mbxItemIdRaw);
                if (mbxItemId != null && mbxItemId.equals(ingredient.getId())) {
                    totalCount += stack.getCount();
                }
            } else if (ingredient.isVanilla()) {
                Item ingredientItem = Registries.ITEM.get(Identifier.of("minecraft", ingredient.getId()));
                if (stack.getItem() == ingredientItem) {
                    totalCount += stack.getCount();
                }
            }

        }
        return totalCount;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        toggleRegions.clear();
        usedInRegions.clear();
        MineboxItem item = isLocked ? lockedItem : itemSupplier.get();
        if (item == null) return;
        if (!item.getId().equals(lastPreloadedItemId)) {
            preloadUsedInTextures(item);
        }

        if (pendingScrollOffset != null) {
            scrollOffset = Math.max(0, pendingScrollOffset);
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        final int lineHeight = 12;
        final int leftMargin = x + UI_PADDING;

        context.getMatrices().pushMatrix();
        context.fill(x - 6, y - 6, x + width + 6, y + height, 0xAA000000);

        int headerBottom = y + HEADER_H;
        context.fill(x, y, x + width, headerBottom, 0x66000000);

        String label = "Qty:";
        int labelW = textRenderer.getWidth(label);
        int fieldW = Math.max(36, textRenderer.getWidth("0000") + 10);
        if (quantityField != null) quantityField.setWidth(fieldW);
        int lockW = (lockButton != null) ? lockButton.getWidth() : 16;
        int clipW = (clipboardButton != null) ? clipboardButton.getWidth() : 16;
        int spacer = 6;

        int rowY = y + (HEADER_H - CTRL_H) / 2;

        int totalW = labelW + spacer + fieldW + spacer + lockW + spacer + clipW;
        int startX = x + (width - totalW) / 2;

        // Quantity label
        int labelX = startX;
        int labelY = y + (HEADER_H - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, Text.of(label), labelX, labelY, 0xFFFFFFFF, false);

        // Quantity text field
        if (quantityField != null) {
            quantityField.setX(labelX + labelW + spacer);
            quantityField.setY(rowY);
        }

        // Lock
        if (lockButton != null) {
            lockButton.setX(quantityField.getX() + fieldW + spacer);
            lockButton.setY(rowY);
        }

        // Set clip btn
        if (clipboardButton != null) {
            clipboardButton.setX(lockButton.getX() + lockW + spacer);
            clipboardButton.setY(rowY);
        }


        context.enableScissor(x, y + HEADER_H, x + width, y + height);
        context.getMatrices().translate(0f, (float) -scrollOffset);

        int drawY = y + HEADER_H + 5;

        // Icon
        Identifier icon = ItemListWidget.ItemEntry.getTexture(item.getId());
        int iconSize = 32;
        if (icon != null) {
            int iconX = x + (width - iconSize) / 2;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, icon,
                    iconX, drawY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize);
        }
        drawY += iconSize + 4;

        // Name
        Text nameText = MineboxItem.getDisplayName(item);
        int nameWidth = textRenderer.getWidth(nameText);
        int nameX = x + (width - nameWidth) / 2;
        context.drawText(textRenderer, nameText, nameX, drawY, 0xFFFFFFFF, false);
        drawY += lineHeight + 2;

        // Lore
        String loreText = MineboxItem.getLoreText(item.getId());
        if (!loreText.isEmpty()) {
            String fullLore = "« " + loreText + " »";
            int loreWidth = textRenderer.getWidth(fullLore);
            int loreX = x + (width - loreWidth) / 2;
            context.drawText(textRenderer, Text.of(fullLore), loreX, drawY, 0xFF6D6D6D, false);
            drawY += lineHeight + 2;
        }

        // Info
        context.drawText(textRenderer, Text.of("Level: " + item.getLevel()), leftMargin, drawY, 0xFFAAAAAA, false);
        drawY += lineHeight;
        context.drawText(textRenderer, Text.of("Category: " + item.getCategory()), leftMargin, drawY, 0xFFAAAAAA, false);
        drawY += lineHeight;
        context.drawText(textRenderer, Text.of("Rarity: " + item.getRarity()), leftMargin, drawY, 0xFFAAAAAA, false);
        drawY += lineHeight;

        // Stats
        if (item.getMbxStats() != null) {
            for (var entry : item.getMbxStats().entrySet()) {
                String stat = entry.getKey() + ": " + entry.getValue().getMin() + " - " + entry.getValue().getMax();
                context.drawText(textRenderer, Text.of(stat), leftMargin, drawY, 0xFFCCCCCC, false);
                drawY += lineHeight;
            }
        }

        // Recipe
        if (item.getRecipe() != null && item.getRecipe().getIngredients() != null) {
            context.drawText(textRenderer, Text.of("Recipe:"), leftMargin, drawY, 0xFFFFD700, false);
            drawY += lineHeight;
            List<MineboxItem.Ingredient> ingredients = item.getRecipe().getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                boolean isLast = i == ingredients.size() - 1;
                drawY = renderRecipeIngredient(
                        context, textRenderer, ingredients.get(i),
                        leftMargin, drawY, 0, quantity, isLast, "root"
                );
            }
            drawY += 4;
        }

        // Used in
        List<MineboxItem> usedIn = usedInCache.computeIfAbsent(item.getId(), key ->
                MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                        .filter(other -> other.getRecipe() != null && other.getRecipe().getIngredients() != null)
                        .filter(other -> other.getRecipe().getIngredients().stream()
                                .anyMatch(ing -> !ing.isVanilla() && item.getId().equals(ing.getId())))
                        .toList()
        );

        if (!usedIn.isEmpty()) {
            context.drawText(textRenderer, Text.of("Used in:"), leftMargin, drawY, 0xFFFFD700, false);
            drawY += lineHeight;

            int iconSizeUsedIn = 16;
            int rowSpacing = 6;
            int textOffset = iconSizeUsedIn + 4;

            for (MineboxItem parent : usedIn) {
                Identifier parentIcon = ItemListWidget.ItemEntry.getTexture(parent.getId());

                if (parentIcon != null) {
                    context.drawTexture(
                            RenderPipelines.GUI_TEXTURED,
                            parentIcon,
                            leftMargin, drawY,
                            0, 0,
                            iconSizeUsedIn, iconSizeUsedIn,
                            iconSizeUsedIn, iconSizeUsedIn
                    );
                }

                Text name = MineboxItem.getDisplayName(parent);
                context.drawText(textRenderer, name, leftMargin + textOffset, drawY + (iconSizeUsedIn - textRenderer.fontHeight) / 2, 0xFFFFFFFF, false);

                int clickableWidth = textOffset + textRenderer.getWidth(name);
                usedInRegions.add(new ClickRegionUsedIn(
                        new ClickRegion(leftMargin, drawY, clickableWidth, iconSizeUsedIn),
                        parent
                ));

                drawY += iconSizeUsedIn + rowSpacing;
            }

            drawY += 4;
        }


        contentHeight = drawY - (y + HEADER_H);
        maxScroll = Math.max(0, contentHeight - (height - HEADER_H));

        if (pendingScrollOffset != null) {
            scrollOffset = Math.max(0, Math.min(pendingScrollOffset, maxScroll));
            pendingScrollOffset = null;
        }

        context.disableScissor();
        context.getMatrices().popMatrix();
    }

    private int renderRecipeIngredient(DrawContext context, TextRenderer textRenderer,
                                       MineboxItem.Ingredient ingredient, int x, int y, int depth,
                                       int amountMultiplier, boolean isLast, String path) {
        final int iconSize = 16;
        final int spacing = 6;
        final int indent = (1 + depth) * 12;

        int amount = ingredient.getAmount() * amountMultiplier;
        boolean hasItem = playerHasIngredient(ingredient, amountMultiplier);
        int ingredientCount = playerIngredientCount(ingredient);
        int stacks = amount / 64;
        int remainder = amount % 64;
        int iconX = x + indent;

        boolean expandable = !ingredient.isVanilla()
                && ingredient.getCustomItem() != null
                && ingredient.getCustomItem().getRecipe() != null
                && ingredient.getCustomItem().getRecipe().getIngredients() != null
                && !ingredient.getCustomItem().getRecipe().getIngredients().isEmpty();

        String key = nodeKey(path, ingredient);

        if (expandable) {
            boolean isCollapsed = !collapsed.contains(key);
            String twisty = isCollapsed ? "▶" : "▼";
            int twistyX = iconX - 10;
            int twistyY = y + (iconSize - textRenderer.fontHeight) / 2;
            context.drawText(textRenderer, Text.of(twisty), twistyX, twistyY, 0xFFCCCCCC, false);
            toggleRegions.put(key, new ClickRegion(twistyX - 2, y, 12, iconSize));
        }

        // icon
        if (ingredient.isVanilla()) {
            ItemStack stack = ingredient.getVanillaStack();
            if (!stack.isEmpty()) {
                context.drawItem(stack, iconX, y);
            }
        } else {
            Identifier icon = ingredient.getTexture();
            if (icon != null) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, iconX, y,
                        0, 0, iconSize, iconSize, iconSize, iconSize);
            }
        }

        int textX = iconX + iconSize + 4;
        int textY = y + (iconSize - textRenderer.fontHeight) / 2;

        MutableText amountText = Text.literal(String.valueOf(amount));
        if (stacks > 0) {
            String stackInfo = (remainder > 0)
                    ? String.format(" (%ds + %d)", stacks, remainder)
                    : String.format(" (%ds)", stacks);
            amountText.append(Text.literal(stackInfo).styled(style -> style.withColor(0xFFaf8e26)));
        }

        amountText.append(Text.literal(" × "))
                .append(ingredient.getDisplayName())
                .append(Text.literal(hasItem ? " ✅" : " ❌ ("+ ingredientCount + "/" + amount + ")")
                        .styled(style -> style.withColor(hasItem ? 0xFF55FF55 : 0xFFFF5555)));

        context.drawText(textRenderer, amountText, textX, textY, 0xFFFFFFFF, false);

        y += iconSize + spacing;

        // Sub ing
        if (expandable && collapsed.contains(key)) {
            MineboxItem subItem = ingredient.getCustomItem();
            var subs = subItem.getRecipe().getIngredients();

            for (int i = 0; i < subs.size(); i++) {
                boolean lastChild = (i == subs.size() - 1);
                String childPath = path + ">" + ingredient.getId() + "#" + i;

                y = renderRecipeIngredient(context, textRenderer, subs.get(i),
                        x, y, depth + 1, amount, lastChild, childPath);
            }
        }

        return y;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        if (mouseX < x || mouseX > x + width || mouseY < y + HEADER_H || mouseY > y + height) return false;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int)(v * SCROLL_STEP), maxScroll));
        if (isLocked) MineboxAdditions.INSTANCE.state.setLockedItemScrollOffset(scrollOffset);
        return true;
    }

    private String nodeKey(String path, MineboxItem.Ingredient ing) {
        String id = ing.getId();
        return path + ">" + (ing.isVanilla() ? "v:" : "c:") + id;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;

        double adjY = mouseY + scrollOffset;

        for (var e : toggleRegions.entrySet()) {
            if (e.getValue().contains(mouseX, adjY)) {
                String key = e.getKey();
                boolean expand = collapsed.contains(key);

                if (Screen.hasShiftDown()) {
                    toggleSubtree(key, expand);
                } else {
                    if (expand) collapsed.remove(key);
                    else collapsed.add(key);
                }

                if (isLocked) {
                    MineboxAdditions.INSTANCE.state.setLockedCollapsedKeys(new HashSet<>(collapsed));
                }
                return true;
            }
        }

        for (ClickRegionUsedIn ui : usedInRegions) {
            if (ui.region.contains(mouseX, adjY)) {
                preloadTextures(ui.target);
                if (onNavigate != null) {
                    onNavigate.accept(ui.target);
                }
                pendingScrollOffset = 0;
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

    @Override
    public void setFocused(boolean focused) { }

    @Override
    public boolean isFocused() { return false; }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) { }

    @Override
    public SelectionType getType() { return SelectionType.HOVERED; }

    public void setControlsVisible(boolean visible) {
        if (quantityField != null) quantityField.setVisible(visible);
        if (lockButton != null) lockButton.visible = visible;
        if (clipboardButton != null) clipboardButton.visible = visible;
    }

    private void preloadTextures(MineboxItem item) {
        if (item == null) return;

        ItemListWidget.ItemEntry.getTextureCache()
                .computeIfAbsent(item.getId(),
                        id -> ItemListWidget.ItemEntry.loadTexture(item.getId(), item.getTexture()));

        if (item.getRecipe() != null && item.getRecipe().getIngredients() != null) {
            for (MineboxItem.Ingredient ing : item.getRecipe().getIngredients()) {
                preloadIngredientTextures(ing);
            }
        }
    }

    private void preloadIngredientTextures(MineboxItem.Ingredient ing) {
        if (ing.isVanilla()) {
            Identifier vanillaId = Identifier.of("minecraft", "textures/item/" + ing.getId() + ".png");
            ItemListWidget.ItemEntry.getTextureCache().putIfAbsent(ing.getId(), vanillaId);
            return;
        }

        MineboxItem sub = ing.getCustomItem();
        if (sub != null) {
            ItemListWidget.ItemEntry.getTextureCache()
                    .computeIfAbsent(sub.getId(),
                            id -> ItemListWidget.ItemEntry.loadTexture(sub.getId(), sub.getTexture()));

            if (sub.getRecipe() != null && sub.getRecipe().getIngredients() != null) {
                for (MineboxItem.Ingredient subIng : sub.getRecipe().getIngredients()) {
                    preloadIngredientTextures(subIng);
                }
            }
        }
    }

    private void preloadUsedInTextures(MineboxItem item) {
        if (item == null) return;
        if (item.getId().equals(lastPreloadedItemId)) return;
        lastPreloadedItemId = item.getId();

        List<MineboxItem> usedIn = usedInCache.computeIfAbsent(item.getId(), key ->
                MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                        .filter(other -> other.getRecipe() != null && other.getRecipe().getIngredients() != null)
                        .filter(other -> other.getRecipe().getIngredients().stream()
                                .anyMatch(ing -> !ing.isVanilla() && item.getId().equals(ing.getId())))
                        .toList()
        );

        for (MineboxItem parent : usedIn) {
            ItemListWidget.ItemEntry.getTextureCache()
                    .computeIfAbsent(parent.getId(),
                            id -> ItemListWidget.ItemEntry.loadTexture(parent.getId(), parent.getTexture()));

            if (parent.getRecipe() != null && parent.getRecipe().getIngredients() != null) {
                for (MineboxItem.Ingredient ing : parent.getRecipe().getIngredients()) {
                    preloadIngredientTextures(ing);
                }
            }
        }
    }


}