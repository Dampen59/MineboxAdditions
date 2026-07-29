package io.dampen59.mineboxadditions.features.atlas;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemDetailPanel;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.*;

public class MineboxAtlasScreen extends Screen {

    private static final int LEFT_PAD  = 10;
    private static final int TOP_PAD   = 6;
    private static final int TITLE_H   = 12;
    private static final int GAP       = 4;
    private static final int SEARCH_H  = 16;
    private static final int CHIP_H    = 14;

    private static final List<String> RARITY_ORDER = List.of(
            "prototype", "contraband", "trash",
            "common", "uncommon", "rare", "epic", "legendary", "mythic"
    );
    private static final String[] SORT_LABELS = { "A→Z", "Lvl ↑", "Lvl ↓" };

    private int panelWidth;
    private int listTop;

    private ItemListWidget itemList;
    private EditBox searchField;
    private List<MineboxItem> allItems;
    private MineboxItem selectedItem = null;
    private ItemDetailPanel itemDetailPanel;

    private final String preSelectItemId;

    private String rarityFilter = null;
    private boolean museumFilter = false;
    private int sortMode = 0;

    private Button rarityButton;
    private Button museumButton;
    private Button sortButton;
    private List<String> availableRarities = new ArrayList<>();

    public MineboxAtlasScreen() { this(null); }

    public MineboxAtlasScreen(String preSelectItemId) {
        super(Component.literal("Items Atlas"));
        this.preSelectItemId = preSelectItemId;
    }

    @Override
    protected void init() {
        Font font = Minecraft.getInstance().font;

        allItems = new ArrayList<>(MineboxAdditions.INSTANCE.state.getMbxItems());

        Set<String> found = new HashSet<>();
        for (MineboxItem item : allItems) {
            if (item.getRarity() != null && !item.getRarity().isBlank())
                found.add(item.getRarity().toLowerCase());
        }
        availableRarities = new ArrayList<>(RARITY_ORDER);
        availableRarities.retainAll(found);
        for (String r : found) {
            if (!availableRarities.contains(r)) availableRarities.add(r);
        }

        int maxTextWidth = allItems.stream()
                .mapToInt(item -> Math.max(
                        font.width(MineboxItem.getDisplayName(item)),
                        font.width("Lvl " + item.getLevel() + " • " + item.getCategory())
                ))
                .max().orElse(120);
        panelWidth = Math.min(200, Math.max(140, 26 + maxTextWidth + 6));

        int searchY = TOP_PAD + TITLE_H + GAP + 3;
        int chipsY  = searchY + SEARCH_H + GAP;
        listTop     = chipsY + CHIP_H + GAP + 2;

        searchField = new EditBox(font, LEFT_PAD, searchY, panelWidth, SEARCH_H, Component.empty());
        searchField.setHint(Component.literal("Search items..."));
        searchField.setMaxLength(64);
        searchField.setResponder(this::updateFilteredItems);
        addRenderableWidget(searchField);

        int sortW = Arrays.stream(SORT_LABELS).mapToInt(font::width).max().orElse(30) + 10;
        int sortX = LEFT_PAD + panelWidth - sortW;
        sortButton = Button.builder(Component.literal(SORT_LABELS[sortMode]), b -> {
            sortMode = (sortMode + 1) % SORT_LABELS.length;
            b.setMessage(Component.literal(SORT_LABELS[sortMode]));
            updateFilteredItems(searchField.getValue());
        }).bounds(sortX, chipsY, sortW, CHIP_H).build();
        addRenderableWidget(sortButton);

        int museumBtnW = font.width("Museum") + 10;
        int museumX = sortX - 4 - museumBtnW;
        museumButton = Button.builder(Component.literal("Museum"), b -> {
            museumFilter = !museumFilter;
            updateFilteredItems(searchField.getValue());
        }).bounds(museumX, chipsY, museumBtnW, CHIP_H).build();
        addRenderableWidget(museumButton);

        List<String> rarityOptions = new ArrayList<>();
        rarityOptions.add(null); // null = All
        rarityOptions.addAll(availableRarities);

        int maxLabelW = rarityOptions.stream()
                .mapToInt(r -> font.width(r == null ? "All" : capitalize(r)))
                .max().orElse(30);
        int rarityBtnW = Math.min(maxLabelW + 10, museumX - 4 - LEFT_PAD);

        String initLabel = rarityFilter == null ? "All" : capitalize(rarityFilter);
        rarityButton = Button.builder(Component.literal(initLabel), b -> {
            int idx = rarityOptions.indexOf(rarityFilter);
            if (idx < 0) idx = -1;
            rarityFilter = rarityOptions.get((idx + 1) % rarityOptions.size());
            b.setMessage(Component.literal(rarityFilter == null ? "All" : capitalize(rarityFilter)));
            updateFilteredItems(searchField.getValue());
        }).bounds(LEFT_PAD, chipsY, rarityBtnW, CHIP_H).build();
        addRenderableWidget(rarityButton);

        itemList = new ItemListWidget(minecraft, LEFT_PAD, listTop, panelWidth, this.height - listTop - 8, 25);
        addRenderableWidget(itemList);

        updateFilteredItems(searchField.getValue());

        int detailX = LEFT_PAD + panelWidth + 12;
        itemDetailPanel = new ItemDetailPanel(
                this::getSelectedItem,
                detailX, TOP_PAD,
                this.width - detailX - 8,
                this.height - TOP_PAD - 8
        );
        addRenderableWidget(itemDetailPanel);
        addWidget(itemDetailPanel);
        itemDetailPanel.setOnNavigate(this::setSelectedItem);
        itemDetailPanel.initLockButton(this);
        itemDetailPanel.setControlsVisible(false);

        if (preSelectItemId != null) {
            allItems.stream()
                    .filter(i -> i.getId().equals(preSelectItemId))
                    .findFirst()
                    .ifPresent(item -> {
                        selectedItem = item;
                        itemDetailPanel.setControlsVisible(true);
                    });
        } else {
            String lockedId = MineboxAdditions.INSTANCE.state.getLockedItemId();
            if (lockedId != null) {
                allItems.stream()
                        .filter(item -> item.getId().equals(lockedId))
                        .findFirst()
                        .ifPresent(locked -> {
                            selectedItem = locked;
                            itemDetailPanel.lock(locked);
                            itemDetailPanel.setControlsVisible(true);
                        });
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        Font font = Minecraft.getInstance().font;
        int total   = allItems != null ? allItems.size() : 0;
        int visible = itemList != null ? itemList.children().size() : 0;

        int divX    = LEFT_PAD + panelWidth + 6;
        int headerH = TOP_PAD + TITLE_H + GAP - 1;

        ctx.fill(0, 0, divX, headerH, 0xCC0D0D0D);

        ctx.fill(0, headerH, divX, this.height, 0x22000000);

        String countStr = visible == total ? total + " items" : visible + "/" + total;
        ctx.text(font, Component.literal("Items Atlas"), LEFT_PAD, TOP_PAD + 1, 0xFFFFFFFF, true);
        int titleW = font.width("Items Atlas");
        ctx.text(font, Component.literal(" · " + countStr), LEFT_PAD + titleW, TOP_PAD + 1, 0xFF777777, false);

        ctx.fill(divX, 0, divX + 1, this.height, 0x33FFFFFF);

        if (rarityButton != null && rarityFilter != null) {
            ctx.fill(rarityButton.getX() - 1, rarityButton.getY() - 1,
                    rarityButton.getX() + rarityButton.getWidth() + 1,
                    rarityButton.getY() + rarityButton.getHeight() + 1,
                    0x5500AAFF);
        }
        if (museumButton != null && museumFilter) {
            ctx.fill(museumButton.getX() - 1, museumButton.getY() - 1,
                    museumButton.getX() + museumButton.getWidth() + 1,
                    museumButton.getY() + museumButton.getHeight() + 1,
                    0x5500AAFF);
        }

        super.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    private void updateFilteredItems(String query) {
        if (itemList == null) return;
        itemList.clearEntries();
        itemList.setScrollAmount(0);

        List<MineboxItem> filtered = new ArrayList<>();
        for (MineboxItem item : allItems) {
            if (matchesRarity(item) && matchesMuseum(item) && matchesQuery(item, query))
                filtered.add(item);
        }

        Comparator<MineboxItem> cmp;
        if (sortMode == 1) cmp = Comparator.comparingInt(MineboxItem::getLevel);
        else if (sortMode == 2) cmp = Comparator.comparingInt(MineboxItem::getLevel).reversed();
        else cmp = Comparator.comparing(i -> MineboxItem.getDisplayName(i).getString());
        filtered.sort(cmp);

        for (MineboxItem item : filtered)
            itemList.addEntry(new ItemListWidget.ItemEntry(item, this));
    }

    private boolean matchesQuery(MineboxItem item, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase();
        return item.getId().toLowerCase().contains(q)
                || item.getCategory().toLowerCase().contains(q)
                || item.getRarity().toLowerCase().contains(q)
                || MineboxItem.getDisplayName(item).getString().toLowerCase().contains(q);
    }

    private boolean matchesRarity(MineboxItem item) {
        return rarityFilter == null || rarityFilter.equalsIgnoreCase(item.getRarity());
    }

    private boolean matchesMuseum(MineboxItem item) {
        if (!museumFilter) return true;
        List<String> missing = MineboxAdditions.INSTANCE.state.getMissingMuseumItemIds();
        return missing != null && missing.contains(item.getId());
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s
                : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public void setSelectedItem(MineboxItem item) {
        this.selectedItem = item;
        itemDetailPanel.setControlsVisible(true);
        if (itemDetailPanel != null) itemDetailPanel.unlock();
    }

    public MineboxItem getSelectedItem() { return selectedItem; }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        if (itemDetailPanel.mouseScrolled(mouseX, mouseY, h, v)) return true;
        return super.mouseScrolled(mouseX, mouseY, h, v);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (itemDetailPanel != null && itemDetailPanel.mouseClicked(event, doubleClick)) return true;
        return super.mouseClicked(event, doubleClick);
    }
}
