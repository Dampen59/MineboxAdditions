package io.dampen59.mineboxadditions.state;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import io.dampen59.mineboxadditions.audio.AudioManager;
import io.dampen59.mineboxadditions.minebox.MineboxChatFlag;
import io.dampen59.mineboxadditions.minebox.MineboxFishingShoal;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.utils.Utils;
import io.socket.client.Socket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.*;

public class State {
    private boolean isConnectedToMinebox = false;
    private boolean loginCommandSent = false;
    private boolean mouseAlertSent = false;
    private boolean bakeryAlertSent = false;
    private boolean buckstarAlertSent = false;
    private boolean cocktailAlertSent = false;

    private String mouseCurrentItemOffer = null;
    private String bakeryCurrentItemOffer = null;
    private String buckstarCurrentItemOffer = null;
    private String cocktailCurrentItemOffer = null;

    private String shopDisplay = null;

    private int currentMoonPhase = -1;

    private List<MineboxItem> mbxItems = null;

    private List<MineboxChatFlag> mbxChatFlags = null;

    private List<MineboxFishingShoal.FishingShoalFish> mbxFishables = new ArrayList<>();

    private final Map<String, Boolean> mbxShiniesUuids = new HashMap<>();

    private Socket objSocket = null;

    private String chatLang = null;

    private AudioManager audioManager = null;

    private final List<Integer> rainTimestamps = new ArrayList<>();
    private final List<Integer> stormTimestamps = new ArrayList<>();

    private boolean performanceMode = false;
    private Map<DisplayEntity.TextDisplayEntity, ArmorStandEntity> performanceModeDebugArmorstands = new HashMap<>();
    private Set<Entity> unrenderedEntities = new HashSet<>();

    public void reset() {
        this.setConnectedToMinebox(false);
        this.setLoginCommandSent(false);
        this.setMouseAlertSent(false);
        this.setBakeryAlertSent(false);
        this.setBuckstarAlertSent(false);
        this.setCocktailAlertSent(false);
        this.setMouseCurrentItemOffer(null);
        this.setBakeryCurrentItemOffer(null);
        this.setBuckstarCurrentItemOffer(null);
        this.setCocktailCurrentItemOffer(null);
        this.setShopDisplay(null);
        this.setCurrentMoonPhase(-1);
        this.setMbxItems(null);
        this.resetShinyList();
        this.setChatLang(null);
        this.setMbxChatFlags(null);
        this.clearWeatherTimestamps();
    }

    public void setConnectedToMinebox(boolean prmValue) {
        this.isConnectedToMinebox = prmValue;
    }

    public boolean getConnectedToMinebox() {
        return this.isConnectedToMinebox;
    }

    public void setLoginCommandSent(boolean prmValue) {
        this.loginCommandSent = prmValue;
    }

    public boolean getLoginCommandSent() {
        return this.loginCommandSent;
    }

    public void setMouseAlertSent(boolean prmValue) {
        this.mouseAlertSent = prmValue;
    }

    public boolean getMouseAlertSent() {
        return this.mouseAlertSent;
    }

    public void setBakeryAlertSent(boolean prmValue) {
        this.bakeryAlertSent = prmValue;
    }

    public boolean getBakeryAlertSent() {
        return this.bakeryAlertSent;
    }

    public void setBuckstarAlertSent(boolean prmValue) {
        this.buckstarAlertSent = prmValue;
    }

    public boolean getBuckstarAlertSent() {
        return this.buckstarAlertSent;
    }

    public void setCocktailAlertSent(boolean prmValue) {
        this.cocktailAlertSent = prmValue;
    }

    public boolean getCocktailAlertSent() {
        return this.cocktailAlertSent;
    }

    public void setMouseCurrentItemOffer(String prmValue) {
        this.mouseCurrentItemOffer = prmValue;
    }

    public String getMouseCurrentItemOffer() {
        return this.mouseCurrentItemOffer;
    }

    public void setBakeryCurrentItemOffer(String prmValue) {
        this.bakeryCurrentItemOffer = prmValue;
    }

    public String getBakeryCurrentItemOffer() {
        return this.bakeryCurrentItemOffer;
    }

    public void setBuckstarCurrentItemOffer(String prmValue) {
        this.buckstarCurrentItemOffer = prmValue;
    }

    public String getBuckstarCurrentItemOffer() {
        return this.buckstarCurrentItemOffer;
    }

    public void setCocktailCurrentItemOffer(String prmValue) {
        this.cocktailCurrentItemOffer = prmValue;
    }

    public String getCocktailCurrentItemOffer() {
        return this.cocktailCurrentItemOffer;
    }

    public void setShopDisplay(String prmValue) {
        this.shopDisplay = prmValue;
    }

    public String getShopDisplay() {
        return this.shopDisplay;
    }

    public void setCurrentMoonPhase(int prmValue) {
        this.currentMoonPhase = prmValue;
    }

    public int getCurrentMoonPhase() {
        return this.currentMoonPhase;
    }

    public void setMbxItems(List<MineboxItem> prmValue) {
        this.mbxItems = prmValue;
    }

    public List<MineboxItem> getMbxItems() {
        return this.mbxItems;
    }

    public void setMbxChatFlags(List<MineboxChatFlag> prmValue) {
        this.mbxChatFlags = prmValue;
    }

    public List<MineboxChatFlag> getMbxChatFlags() {
        return this.mbxChatFlags;
    }

    public void setMbxFishables(List<MineboxFishingShoal.FishingShoalFish> prmValue) {
        this.mbxFishables = prmValue;
    }

    public List<MineboxFishingShoal.FishingShoalFish> getMbxFishables() {
        return this.mbxFishables;
    }

    public Map<String, Boolean> getMbxShiniesUuids() {
        return this.mbxShiniesUuids;
    }

    public void resetShinyList() {
        this.mbxShiniesUuids.clear();
    }

    public void addShinyUuid(String prmUuid) {
        this.mbxShiniesUuids.put(prmUuid, false);
    }

    public void setChatLang(String prmValue) {
        this.chatLang = Utils.actionBarDataToChatLang(prmValue);
    }

    public String getChatLang() {
        return this.chatLang;
    }

    public void setSocket(Socket prmValue) {
        this.objSocket = prmValue;
    }

    public Socket getSocket() {
        return this.objSocket;
    }

    public void setAudioManager(AudioManager prmValue) {
        this.audioManager = prmValue;
    }

    public AudioManager getAudioManager() {
        return this.audioManager;
    }

    public void addRainTimestamp(Integer prmValue) {
        if (!this.rainTimestamps.contains(prmValue)) this.rainTimestamps.add(prmValue);
    }

    public void addStormTimestamp(Integer prmValue) {
        if (!this.stormTimestamps.contains(prmValue)) this.stormTimestamps.add(prmValue);
    }

    public List<Integer> getRainTimestamps() {
        return rainTimestamps;
    }

    public List<Integer> getStormTimestamps() {
        return stormTimestamps;
    }

    public void clearWeatherTimestamps() {
        this.rainTimestamps.clear();
        this.stormTimestamps.clear();
    }

    public void setPerformanceMode(boolean prmValue) { this.performanceMode = prmValue; }
    public boolean getPerformanceMode() { return this.performanceMode; }

    public Map<DisplayEntity.TextDisplayEntity, ArmorStandEntity> getPerformanceModeDebugArmorstands() { return this.performanceModeDebugArmorstands; }
    public Set<Entity> getUnrenderedEntities() { return this.unrenderedEntities; }
}
