package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.features.bestiary.BestiaryEntry;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.features.item.Insect;
import net.minecraft.network.chat.Component;

import java.util.*;

public class State {
    private final WeatherState weatherState = new WeatherState();

    private String shopDisplay = null;
    private int currentMoonPhase = -1;
    private List<MineboxItem> mbxItems = null;
    private Set<String> auctionCatalogIds = new HashSet<>();
    private List<BestiaryEntry> mbxBestiary = null;
    private List<Insect> insects = null;
    private Map<String, Insect> insectById = new HashMap<>();
    private Map<String, List<Harvestable>> mbxHarvestables = new HashMap<>();
    private List<FishingShoal.Item> shoalItems = new ArrayList<>();
    private final Map<String, Boolean> mbxShiniesUuids = new HashMap<>();

    private String lastSentCommand = null;
    private String lockedItemId = null;
    private int lockedItemQuantity = 1;
    private Integer lockedItemScrollOffset = null;
    private Set<String> lockedCollapsedKeys = new HashSet<>();

    private List<String> missingMuseumItemIds = new ArrayList<>();

    private Component bossbarIsland = null;
    private Component bossbarTime = null;
    private Component bossbarKeyFragment = null;
    private Component bossbarStatsPoints = null;
    private Component bossbarFreeItem = null;
    private Component bossbarVotes = null;

    private final Map<String, String> entityTextCache = new HashMap<>();

    public String getShopDisplay() { return shopDisplay; }
    public void setShopDisplay(String display) { this.shopDisplay = display; }

    public int getCurrentMoonPhase() { return currentMoonPhase; }
    public void setCurrentMoonPhase(int phase) { this.currentMoonPhase = phase; }

    public List<MineboxItem> getMbxItems() { return mbxItems; }
    public void setMbxItems(List<MineboxItem> items) { this.mbxItems = items; }

    public Set<String> getAuctionCatalogIds() { return auctionCatalogIds; }
    public void setAuctionCatalogIds(Set<String> ids) {
        this.auctionCatalogIds = (ids == null) ? new HashSet<>() : ids;
    }

    public List<BestiaryEntry> getMbxBestiary() { return mbxBestiary; }
    public void setMbxBestiary(List<BestiaryEntry> bestiary) { this.mbxBestiary = bestiary; }

    public List<Insect> getInsects() { return insects; }
    public Insect getInsectById(String id) { return insectById.get(id); }
    public void setInsects(List<Insect> list) {
        this.insects = list;
        this.insectById = new HashMap<>();
        if (list != null) list.forEach(i -> insectById.put(i.getId(), i));
    }

    public List<Harvestable> getMineboxHarvestables(String islandName) {
        return mbxHarvestables.get(islandName);
    }

    public Set<String> getAllHarvestableKeys() {
        return mbxHarvestables.keySet();
    }

    public void addMineboxHarvestables(String islandName, List<Harvestable> data) {
        if (mbxHarvestables.containsKey(islandName)) mbxHarvestables.remove(islandName);
        mbxHarvestables.put(islandName, data);
    }

    public Map<String, Boolean> getMbxShiniesUuids() { return mbxShiniesUuids; }
    public void resetShinyList() { mbxShiniesUuids.clear(); }
    public void addShinyUuid(String uuid) { mbxShiniesUuids.put(uuid, false); }

    public WeatherState getWeatherState() { return weatherState; }

    public void setLastSentCommand(String cmd) { this.lastSentCommand = cmd; }
    public String getLastSentCommand() { return lastSentCommand; }

    public void setLockedItemId(String id) { this.lockedItemId = id; }
    public String getLockedItemId() { return lockedItemId; }
    public boolean hasLockedItem() { return lockedItemId != null; }

    public void setLockedItemQuantity(int quantity) {
        if (quantity > 0) this.lockedItemQuantity = quantity;
    }

    public int getLockedItemQuantity() { return lockedItemQuantity; }

    public void setLockedItemScrollOffset(Integer offset) {
        this.lockedItemScrollOffset = offset;
    }

    public Integer getLockedItemScrollOffset() {
        return this.lockedItemScrollOffset;
    }

    public Set<String> getLockedCollapsedKeys() {
        return lockedCollapsedKeys;
    }

    public void setLockedCollapsedKeys(Set<String> keys) {
        lockedCollapsedKeys = (keys == null) ? new HashSet<>() : new HashSet<>(keys);
    }

    public MineboxItem getItemById(String id) {
        if (mbxItems == null) return null;
        return mbxItems.stream().filter(i -> id.equals(i.getId())).findFirst().orElse(null);
    }

    public void cacheEntityText(String uuid, String text) {
        entityTextCache.put(uuid, text);
    }

    public String getCachedEntityText(String uuid) {
        return entityTextCache.get(uuid);
    }

    public boolean hasEntityTextCached(String uuid) {
        return entityTextCache.containsKey(uuid);
    }

    public void cleanStaleEntityTextCache(Set<String> liveUuids) {
        entityTextCache.keySet().removeIf(uuid -> !liveUuids.contains(uuid));
    }

    public List<String> getMissingMuseumItemIds() {
        return missingMuseumItemIds;
    }

    public void setMissingMuseumItemIds(List<String> ids) {
        this.missingMuseumItemIds = (ids == null) ? new ArrayList<>() : new ArrayList<>(ids);
    }

    public Component getBossbarIsland() { return bossbarIsland; }
    public void setBossbarIsland(Component c) { this.bossbarIsland = c; }

    public Component getBossbarTime() { return bossbarTime; }
    public void setBossbarTime(Component c) { this.bossbarTime = c; }

    public Component getBossbarKeyFragment() { return bossbarKeyFragment; }
    public void setBossbarKeyFragment(Component c) { this.bossbarKeyFragment = c; }

    public Component getBossbarStatsPoints() { return bossbarStatsPoints; }
    public void setBossbarStatsPoints(Component c) { this.bossbarStatsPoints = c; }

    public Component getBossbarFreeItem() { return bossbarFreeItem; }
    public void setBossbarFreeItem(Component c) { this.bossbarFreeItem = c; }

    public Component getBossbarVotes() { return bossbarVotes; }
    public void setBossbarVotes(Component c) { this.bossbarVotes = c; }

    public void reset() {
        ShopManager.reset();
        setShopDisplay(null);
        setCurrentMoonPhase(-1);
        resetShinyList();
        weatherState.clear();
        missingMuseumItemIds.clear();
        bossbarIsland = null;
        bossbarTime = null;
        bossbarKeyFragment = null;
        bossbarStatsPoints = null;
        bossbarFreeItem = null;
        bossbarVotes = null;
    }
}