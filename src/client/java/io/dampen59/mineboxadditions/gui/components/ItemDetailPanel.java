package io.dampen59.mineboxadditions.gui.components;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
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
    private int quantity = 1;
    private final Set<String> collapsed = new HashSet<>();
    private final Map<String, ClickRegion> toggleRegions = new HashMap<>();


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

    public void initLockButton(Screen parent) {
        int buttonWidth = 12;
        int buttonHeight = 12;
        int buttonX = x + width - buttonWidth - 6;
        int buttonY = y - buttonHeight - 4;

        lockButton = ButtonWidget.builder(Text.of("🔒"), btn -> {
            isLocked = !isLocked;
            lockButton.setMessage(Text.of(isLocked ? "🔒" : "🔓"));
            if (isLocked) {
                lockedItem = itemSupplier.get();
                if (lockedItem != null) {
                    MineboxAdditions.INSTANCE.modState.setLockedItemId(lockedItem.getId());
                    MineboxAdditions.INSTANCE.modState.setLockedItemScrollOffset(scrollOffset);
                }
            } else {
                unlock();
            }
        }).dimensions(buttonX, buttonY, buttonWidth, buttonHeight).build();

        parent.addDrawableChild(lockButton);

        quantityField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                x + 6, y - 16, 40, 12,
                Text.of("Qty"));
        quantityField.setMaxLength(4);
        quantityField.setText("1");
        quantityField.setChangedListener(text -> {
            try {
                int newQty = Integer.parseInt(text.trim());
                if (newQty > 0) {
                    quantity = newQty;
                    if (isLocked && lockedItem != null) {
                        MineboxAdditions.INSTANCE.modState.setLockedItemQuantity(quantity);
                    }
                }
            } catch (NumberFormatException ignored) {
                // bah alors chef, t'es défoncé pour pas savoir écrire un chiffre ?
            }
        });

        parent.addDrawableChild(quantityField);
    }

    public void unlock() {
        isLocked = false;
        lockedItem = null;
        quantity = 1;
        if (quantityField != null) quantityField.setText("1");
        if (lockButton != null) lockButton.setMessage(Text.of("🔓"));

        MineboxAdditions.INSTANCE.modState.setLockedItemId(null);
        MineboxAdditions.INSTANCE.modState.setLockedItemScrollOffset(null);

        // ⬇️ Optional: clear persisted collapsed state
        MineboxAdditions.INSTANCE.modState.setLockedCollapsedKeys(Collections.emptySet());
    }

    public void lock(MineboxItem item) {
        this.isLocked = true;
        this.lockedItem = item;

        if (lockButton != null) lockButton.setMessage(Text.of("🔒"));

        int savedQty = MineboxAdditions.INSTANCE.modState.getLockedItemQuantity();
        quantity = savedQty > 0 ? savedQty : 1;
        if (quantityField != null) quantityField.setText(String.valueOf(quantity));
        MineboxAdditions.INSTANCE.modState.setLockedItemQuantity(quantity);

        Integer savedScroll = MineboxAdditions.INSTANCE.modState.getLockedItemScrollOffset();
        if (savedScroll != null) pendingScrollOffset = savedScroll;

        // ⬇️ Restore collapsed state
        collapsed.clear();
        collapsed.addAll(MineboxAdditions.INSTANCE.modState.getLockedCollapsedKeys());
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

            if (ingredient.isVanilla()) {
                Item ingredientItem = Registries.ITEM.get(Identifier.of("minecraft", ingredient.getId()));
                if (stack.getItem() == ingredientItem) {
                    totalCount += stack.getCount();
                    if (totalCount >= requiredAmount)
                        return true;
                }
            } else {
                if (Utils.isMineboxItem(stack)) {
                    String mbxItemId = Utils.getMineboxItemId(stack);
                    if (mbxItemId != null && mbxItemId.equals(ingredient.getId())) {
                        totalCount += stack.getCount();
                        if (totalCount >= requiredAmount)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        toggleRegions.clear();
        MineboxItem item = isLocked ? lockedItem : itemSupplier.get();
        if (item == null)
            return;

        if (pendingScrollOffset != null)
            scrollOffset = Math.max(0, pendingScrollOffset);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        final int lineHeight = 12;
        final int leftMargin = x + 6;

        context.getMatrices().pushMatrix();
        context.fill(x - 6, y - 6, x + width + 6, y + height, 0xAA000000);
        context.enableScissor(x, y, x + width, y + height);
        context.getMatrices().translate(0f, (float) -scrollOffset);

        int drawY = y + 5;

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

        // Used in
        List<MineboxItem> usedIn = MineboxAdditions.INSTANCE.modState.getMbxItems().stream()
                .filter(other -> other.getRecipe() != null && other.getRecipe().getIngredients() != null)
                .filter(other -> other.getRecipe().getIngredients().stream()
                        .anyMatch(ing -> !ing.isVanilla() && item.getId().equals(ing.getId())))
                .toList();

        if (!usedIn.isEmpty()) {
            context.drawText(textRenderer, Text.of("Used in:"), leftMargin, drawY, 0xFFFFD700, false);
            drawY += lineHeight;

            for (MineboxItem parent : usedIn) {
                context.drawText(textRenderer, Text.literal("→ ").append(MineboxItem.getDisplayName(parent)),
                        leftMargin + 8, drawY, 0xFFFFFFFF, false);
                drawY += lineHeight;
            }

            drawY += 4;
        }

        // Recipe
        if (item.getRecipe() != null && item.getRecipe().getIngredients() != null) {
            context.drawText(textRenderer, Text.of("Recipe:"), leftMargin, drawY, 0xFFFFD700, false);
            drawY += lineHeight;
            List<MineboxItem.Ingredient> ingredients = item.getRecipe().getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                boolean isLast = i == ingredients.size() - 1;
                drawY = renderRecipeIngredient(context, textRenderer, ingredients.get(i),
                        leftMargin, drawY, 0, quantity, isLast, "root");
            }
        }

        contentHeight = drawY - y;
        maxScroll = Math.max(0, contentHeight - height);

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
            boolean isCollapsed = collapsed.contains(key);
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
                .append(Text.literal(hasItem ? " ✅" : " ❌")
                        .styled(style -> style.withColor(hasItem ? 0xFF55FF55 : 0xFFFF5555)));

        context.drawText(textRenderer, amountText, textX, textY, 0xFFFFFFFF, false);

        y += iconSize + spacing;

        // Sub ing
        if (expandable && !collapsed.contains(key)) {
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
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height)
            return false;
        scrollOffset -= v * SCROLL_STEP;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        if (isLocked) {
            MineboxAdditions.INSTANCE.modState.setLockedItemScrollOffset(scrollOffset);
        }
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
                    MineboxAdditions.INSTANCE.modState.setLockedCollapsedKeys(new HashSet<>(collapsed));
                }
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
}