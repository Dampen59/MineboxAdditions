package io.dampen59.mineboxadditions.features.bestiary;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public class BestiaryEntry {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("family")
    private String family;

    @JsonProperty("type")
    private String type;

    @JsonProperty("level")
    private int level;

    @JsonProperty("level_max")
    private int levelMax;

    @JsonProperty("health")
    private List<Integer> health;

    @JsonProperty("image")
    private String image;

    @JsonProperty("zones")
    private List<String> zones;

    @JsonProperty("drops")
    private List<BestiaryDrop> drops;

    @JsonProperty("stats")
    private Map<String, List<Integer>> stats;

    @JsonProperty("resistances")
    private Map<String, List<Integer>> resistances;

    public String getId() { return id; }
    public String getName() { return Component.translatable("mbx.bestiary." + getId()).getString(); }
    public String getFamily() { return family; }
    public String getType() { return type; }
    public int getLevel() { return level; }
    public int getLevelMax() { return levelMax; }
    public List<Integer> getHealth() { return health; }
    public String getImage() { return image; }
    public List<String> getZones() { return zones; }
    public List<BestiaryDrop> getDrops() { return drops; }
    public Map<String, List<Integer>> getStats() { return stats; }
    public Map<String, List<Integer>> getResistances() { return resistances; }
}
