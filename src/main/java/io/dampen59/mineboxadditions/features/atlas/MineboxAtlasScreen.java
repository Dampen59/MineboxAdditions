package io.dampen59.mineboxadditions.features.atlas;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemDetailPanel;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MineboxAtlasScreen extends Screen {
    private ItemListWidget itemList;

    private TextFieldWidget searchField;
    private List<MineboxItem> allItems;

    private MineboxItem selectedItem = null;

    private ItemDetailPanel itemDetailPanel;


    public MineboxAtlasScreen() {
        super(Text.of("Minebox Atlas"));
    }

    @Override
    protected void init() {
        int left = 10;
        int top = 45;
        int height = this.height - 100;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int maxTextWidth = MineboxAdditions.INSTANCE.state.getMbxItems().stream()
                .map(item -> {
                    String line1 = Text.translatable("mbx.items." + item.getId() + ".name").getString();
                    String line2 = Text.translatable("mineboxadditions.gui.atlas.level_short", item.getLevel()).getString() + " • " + item.getCategory();
                    return Math.max(textRenderer.getWidth(line1), textRenderer.getWidth(line2));
                })
                .max(Integer::compare)
                .orElse(0);

        int panelWidth = 24 + maxTextWidth + 10;

        searchField = new TextFieldWidget(textRenderer, left, 15, panelWidth, 20, Text.of("Search..."));
        searchField.setChangedListener(this::updateFilteredItems);
        this.addDrawableChild(searchField);

        itemList = new ItemListWidget(client, left, top, panelWidth, height, 25);
        this.addDrawableChild(itemList);

        this.allItems = new ArrayList<>(MineboxAdditions.INSTANCE.state.getMbxItems());

        updateFilteredItems(searchField.getText());

        int detailX = itemList.getX() + itemList.getWidth() + 20;

        itemDetailPanel = new ItemDetailPanel(this::getSelectedItem, detailX, 20, this.width - panelWidth - 50, height + 70);
        this.addDrawableChild(itemDetailPanel);
        this.addSelectableChild(itemDetailPanel);
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //this.renderBackground(context, mouseX, mouseY, delta);
        //searchField.render(context, mouseX, mouseY, delta);
        //itemList.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }


    private void updateFilteredItems(String query) {
        itemList.clearEntries();
        itemList.setScrollY(0);

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (itemDetailPanel != null && itemDetailPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


}
