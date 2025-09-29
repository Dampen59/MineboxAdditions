package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.audio.AudioManager;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.socket.client.Socket;

import java.util.*;

public class State {
    private final OfferState offerState = new OfferState();
    private final WeatherState weatherState = new WeatherState();
    private final HUDState hudState = new HUDState();

    private boolean connectedToMinebox = false;
    private boolean loginCommandSent = false;

    private String shopDisplay = null;
    private int currentMoonPhase = -1;
    private List<MineboxItem> mbxItems = null;
    private Map<String, List<Harvestable>> mbxHarvestables = new HashMap<>();
    private List<FishingShoal.Item> shoalItems = new ArrayList<>();
    private final Map<String, Boolean> mbxShiniesUuids = new HashMap<>();

    private Socket socket = null;
    private AudioManager audioManager = null;

    private String lastSentCommand = null;
    private String lockedItemId = null;
    private int lockedItemQuantity = 1;
    private Integer lockedItemScrollOffset = null;
    private Set<String> lockedCollapsedKeys = new HashSet<>();

    private List<String> missingMuseumItemIds = new ArrayList<>();

    private final Map<String, String> entityTextCache = new HashMap<>();

    private final OfferState.MermaidItemOffer mermaidItemOffer = new OfferState.MermaidItemOffer();

    public boolean isConnectedToMinebox() { return connectedToMinebox; }
    public void setConnectedToMinebox(boolean value) { this.connectedToMinebox = value; }

    public boolean isLoginCommandSent() { return loginCommandSent; }
    public void setLoginCommandSent(boolean value) { this.loginCommandSent = value; }

    public String getShopDisplay() { return shopDisplay; }
    public void setShopDisplay(String display) { this.shopDisplay = display; }

    public int getCurrentMoonPhase() { return currentMoonPhase; }
    public void setCurrentMoonPhase(int phase) { this.currentMoonPhase = phase; }

    public List<MineboxItem> getMbxItems() { return mbxItems; }
    public void setMbxItems(List<MineboxItem> items) { this.mbxItems = items; }

    public List<Harvestable> getMineboxHarvestables(String islandName) {
        return mbxHarvestables.get(islandName);
    }

    public void addMineboxHarvestables(String islandName, List<Harvestable> data) {
        if (mbxHarvestables.containsKey(islandName)) mbxHarvestables.remove(islandName);
        mbxHarvestables.put(islandName, data);
    }

    public List<FishingShoal.Item> getShoalItems() { return shoalItems; }
    public void setShoalItems(List<FishingShoal.Item> shoalItems) {
        this.shoalItems = shoalItems;
    }

    public Map<String, Boolean> getMbxShiniesUuids() { return mbxShiniesUuids; }
    public void resetShinyList() { mbxShiniesUuids.clear(); }
    public void addShinyUuid(String uuid) { mbxShiniesUuids.put(uuid, false); }

    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }

    public AudioManager getAudioManager() { return audioManager; }
    public void setAudioManager(AudioManager audioManager) { this.audioManager = audioManager; }

    public OfferState getOfferState() { return offerState; }
    public WeatherState getWeatherState() { return weatherState; }
    public HUDState getHUDState() { return hudState; }
    public OfferState.MermaidItemOffer getMermaidItemOffer() { return mermaidItemOffer; }

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
        setConnectedToMinebox(false);
        setLoginCommandSent(false);
        offerState.reset();
        setShopDisplay(null);
        setCurrentMoonPhase(-1);
        setMbxItems(null);
        resetShinyList();
        weatherState.clear();
        missingMuseumItemIds.clear();
    }
}