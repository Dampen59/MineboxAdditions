package io.dampen59.mineboxadditions.features.atlas;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemDetailPanel;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import net.minecraft.client.input.MouseButtonEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MineboxAtlasScreen extends Screen {
    private ItemListWidget itemList;

    private EditBox searchField;
    private List<MineboxItem> allItems;

    private MineboxItem selectedItem = null;

    private ItemDetailPanel itemDetailPanel;


    public MineboxAtlasScreen() {
        super(Component.literal("Minebox Atlas"));
    }

    @Override
    protected void init() {
        int left = 10;
        int top = 45;
        int height = this.height - 100;

        Font textRenderer = Minecraft.getInstance().font;

        int maxTextWidth = MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                .map(item -> {
                    String line1 = Component.translatable("mbx.items." + item.getId() + ".name").getString();
                    String line2 = Component.translatable("mineboxadditions.gui.atlas.level_short", item.getLevel()).getString() + " • " + item.getCategory();
                    return Math.max(textRenderer.width(line1), textRenderer.width(line2));
                })
                .max(Comparator.naturalOrder())
                .orElse(0);

        int panelWidth = 24 + maxTextWidth + 10;

        searchField = new EditBox(textRenderer, left, 15, panelWidth, 20, Component.literal("Search..."));
        searchField.setResponder(this::updateFilteredItems);
        this.addRenderableWidget(searchField);

        itemList = new ItemListWidget(minecraft, left, top, panelWidth, height, 25);
        this.addRenderableWidget(itemList);

        this.allItems = new ArrayList<>(MineboxAdditions.INSTANCE.state.getMbxItems());

        updateFilteredItems(searchField.getValue());

        int detailX = itemList.getX() + itemList.getWidth() + 20;

        itemDetailPanel = new ItemDetailPanel(this::getSelectedItem, detailX, 20, this.width - panelWidth - 50, height + 70);
        this.addRenderableWidget(itemDetailPanel);
        this.addWidget(itemDetailPanel);
        itemDetailPanel.setOnNavigate(this::setSelectedItem);
        itemDetailPanel.initLockButton(this);
        itemDetailPanel.setControlsVisible(false);
        String lockedId = MineboxAdditions.INSTANCE.state.getLockedItemId();
        if (lockedId != null) {
            MineboxItem locked = allItems.stream()
                    .filter(item -> item.getId().equals(lockedId))
                    .findFirst()
                    .orElse(null);
            if (locked != null) {
                selectedItem = locked;
                itemDetailPanel.lock(locked);
                itemDetailPanel.setControlsVisible(true);
            }
        }

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        //this.renderBackground(context, mouseX, mouseY, delta);
        //searchField.render(context, mouseX, mouseY, delta);
        //itemList.render(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }


    private void updateFilteredItems(String query) {
        itemList.clearEntries();
        itemList.setScrollAmount(0);

        for (MineboxItem item : allItems) {
            if (matchesQuery(item, query)) {
                itemList.addEntry(new ItemListWidget.ItemEntry(item, this));
            }
        }
    }

    private boolean matchesQuery(MineboxItem item, String query) {
        String q = query.toLowerCase();
        return item.getId().toLowerCase().contains(q)
                || item.getCategory().toLowerCase().contains(q)
                || item.getRarity().toLowerCase().contains(q)
                || MineboxItem.getDisplayName(item).getString().toLowerCase().contains(q);
    }

    public void setSelectedItem(MineboxItem item) {
        this.selectedItem = item;
        itemDetailPanel.setControlsVisible(true);
        if (itemDetailPanel != null) {
            itemDetailPanel.unlock();
        }
    }


    public MineboxItem getSelectedItem() {
        return selectedItem;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (itemDetailPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (itemDetailPanel != null && itemDetailPanel.mouseClicked(event, doubleClick)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }


}
