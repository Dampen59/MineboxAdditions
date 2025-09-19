package io.dampen59.mineboxadditions.gui.components;

import io.dampen59.mineboxadditions.gui.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemListWidget extends EntryListWidget<ItemListWidget.ItemEntry> {
    private final int left;

    public ItemListWidget(MinecraftClient client, int left, int top, int width, int forcedHeight, int itemHeight) {
        super(client, width, top + forcedHeight, top, itemHeight);
        this.left = left;
        this.setX(left);
    }

    @Override
    public int getRowLeft() {
        return left;
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 6;
    }

    @Override
    protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) { }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    public static class ItemEntry extends Entry<ItemEntry> {
        private final MineboxItem item;
        private static final Map<String, Identifier> textureCache = new HashMap<>();
        private final MineboxAtlasScreen parent;

        public ItemEntry(MineboxItem item, MineboxAtlasScreen parent) {
            this.item = item;
            this.parent = parent;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            MinecraftClient client = MinecraftClient.getInstance();

            context.getMatrices().pushMatrix();

            boolean isSelected = parent.getSelectedItem() == item;

            int backgroundColor = 0x00000000;
            if (isSelected) {
                backgroundColor = 0x5544AAFF;
            } else if (hovered) {
                backgroundColor = 0x33FFFFFF;
            }

            context.fill(x, y, x + entryWidth, y + entryHeight + 4, backgroundColor);

            Identifier icon = textureCache.computeIfAbsent(item.getId(), id -> loadTexture(item.getId(), item.getTexture()));
            if (icon != null) {
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        icon,
                        x + 4, y + 4,
                        0, 0,
                        16, 16,
                        16, 16
                );
            }

            context.drawText(client.textRenderer, MineboxItem.getDisplayName(item), x + 24, y + 4, 0xFFFFFFFF, false);
            context.drawText(client.textRenderer, Text.of("Lvl " + item.getLevel() + " • " + item.getCategory()),
                    x + 24, y + 14, 0xFFAAAAAA, false);

            context.getMatrices().popMatrix();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            parent.setSelectedItem(item);
            preloadIngredientTextures(item);
            return true;
        }

        @Nullable
        private static Identifier loadTexture(String id, String base64) {
            try {
                String textureName = "textures/items/" + id + ".png";
                return ImageUtils.createTextureFromBase64(base64, textureName);
            } catch (Exception e) {
                System.err.println("Failed to decode texture for item " + id);
                e.printStackTrace();
                return null;
            }
        }

        private void preloadIngredientTextures(MineboxItem item) {
            if (item.getRecipe() == null || item.getRecipe().getIngredients() == null) return;

            for (MineboxItem.Ingredient ingredient : item.getRecipe().getIngredients()) {
                if (!ingredient.isVanilla()) {
                    MineboxItem subItem = ingredient.getCustomItem();
                    if (subItem != null) {
                        textureCache.computeIfAbsent(subItem.getId(),
                                id -> loadTexture(id, subItem.getTexture()));
                        preloadIngredientTextures(subItem);
                    }
                } else {
                    Identifier vanillaId = Identifier.of("minecraft", "textures/item/" + ingredient.getId() + ".png");
                    textureCache.putIfAbsent(ingredient.getId(), vanillaId);
                }
            }
        }

        public static Identifier getTexture(String id) {
            return textureCache.get(id);
        }

        public static Map<String, Identifier> getTextureCache() {
            return textureCache;
        }
    }
}
