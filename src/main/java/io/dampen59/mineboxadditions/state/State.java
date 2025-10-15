package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.voicechat.AudioManager;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.features.item.MineboxItem;

import java.util.*;

public class State {
    private final WeatherState weatherState = new WeatherState();

    private String shopDisplay = null;
    private int currentMoonPhase = -1;
    private List<MineboxItem> mbxItems = null;
    private Map<String, List<Harvestable>> mbxHarvestables = new HashMap<>();
    private List<FishingShoal.Item> shoalItems = new ArrayList<>();
    private final Map<String, Boolean> mbxShiniesUuids = new HashMap<>();

    private AudioManager audioManager = null;

    private String lastSentCommand = null;
    private String lockedItemId = null;
    private int lockedItemQuantity = 1;
    private Integer lockedItemScrollOffset = null;
    private Set<String> lockedCollapsedKeys = new HashSet<>();

    private List<String> missingMuseumItemIds = new ArrayList<>();

    private final Map<String, String> entityTextCache = new HashMap<>();

    public String getShopDisplay() { return shopDisplay; }
    public void setShopDisplay(String display) { this.shopDisplay = display; }

    public int getCurrentMoonPhase() { return currentMoonPhase; }
    public void setCurrentMoonPhase(int phase) { this.currentMoonPhase = phase; }

    public List<Harvestable> getMineboxHarvestables(String islandName) {
        return mbxHarvestables.get(islandName);
    }

    public void addMineboxHarvestables(String islandName, List<Harvestable> data) {
        if (mbxHarvestables.containsKey(islandName)) mbxHarvestables.remove(islandName);
        mbxHarvestables.put(islandName, data);
    }

    public Map<String, Boolean> getMbxShiniesUuids() { return mbxShiniesUuids; }
    public void resetShinyList() { mbxShiniesUuids.clear(); }
    public void addShinyUuid(String uuid) { mbxShiniesUuids.put(uuid, false); }

    public AudioManager getAudioManager() { return audioManager; }
    public void setAudioManager(AudioManager audioManager) { this.audioManager = audioManager; }

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

    public void reset() {
        ShopManager.reset();
        setShopDisplay(null);
        setCurrentMoonPhase(-1);
        resetShinyList();
        weatherState.clear();
        missingMuseumItemIds.clear();
    }
}