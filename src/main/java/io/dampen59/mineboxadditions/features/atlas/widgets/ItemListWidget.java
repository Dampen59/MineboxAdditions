package io.dampen59.mineboxadditions.features.atlas.widgets;

import io.dampen59.mineboxadditions.features.atlas.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemListWidget extends AbstractSelectionList<ItemListWidget.ItemEntry> {
    private final int left;

    public ItemListWidget(Minecraft client, int left, int top, int width, int forcedHeight, int itemHeight) {
        super(client, width, top + forcedHeight, top, itemHeight);
        this.left = left;
        this.setX(left);
    }

    public int getRowLeft() {
        return left;
    }

    protected int getScrollbarX() {
        return this.getX() + this.width - 6;
    }

    public int getRowWidth() {
        return this.width - 6;
    }

    protected void drawSelectionHighlight(GuiGraphicsExtractor context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) { }

    protected void appendClickableNarrations(NarrationElementOutput builder) { }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) { }

    public static class ItemEntry extends Entry<ItemEntry> {
        private final MineboxItem item;
        private static final Map<String, Identifier> textureCache = new HashMap<>();
        private final MineboxAtlasScreen parent;

        public ItemEntry(MineboxItem item, MineboxAtlasScreen parent) {
            this.item = item;
            this.parent = parent;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Minecraft client = Minecraft.getInstance();
            int rowWidth = 200;
            int rowHeight = 25;

            boolean isSelected = parent.getSelectedItem() == item;
            int backgroundColor = isSelected ? 0x5544AAFF : hovered ? 0x33FFFFFF : 0x00000000;
            context.fill(0, 0, rowWidth, rowHeight, backgroundColor);

            Identifier icon = textureCache.computeIfAbsent(item.getId(), id -> loadTexture(item.getId(), item.getTexture()));
            if (icon != null) {
                context.blit(RenderPipelines.GUI_TEXTURED, icon, 4, 4, 0, 0, 16, 16, 16, 16);
            }

            context.text(client.font, MineboxItem.getDisplayName(item), 24, 4, 0xFFFFFFFF, false);
            context.text(client.font, Component.literal("Lvl " + item.getLevel() + " • " + item.getCategory()),
                    24, 14, 0xFFAAAAAA, false);
        }

        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
            parent.setSelectedItem(item);
            preloadIngredientTextures(item);
            return true;
        }

        @Nullable
        public static Identifier loadTexture(String id, String base64) {
            try {
                String textureName = "textures/items/" + id + ".png";
                return ImageUtils.createTextureFromBase64(base64, textureName);
            } catch (Exception e) {
                System.err.println("Failed to decode texture for item " + id);
                e.printStackTrace();
                return null;
            }
        }

        public void preloadIngredientTextures(MineboxItem item) {
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
                    Identifier vanillaId = Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + ingredient.getId() + ".png");
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
