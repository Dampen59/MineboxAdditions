package io.dampen59.mineboxadditions.minebox;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;

public class MineboxItem {
    @JsonProperty("id")
    private String id;

    @JsonProperty("mbxStats")
    private Map<String, MineboxStat> mbxStats;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, MineboxStat> getMbxStats() {
        return mbxStats;
    }

    public void setMbxStats(Map<String, MineboxStat> mbxStats) {
        this.mbxStats = mbxStats;
    }

    public Optional<MineboxStat> getStat(String statName) {
        return mbxStats == null
                ? Optional.empty()
                : Optional.ofNullable(mbxStats.get(statName));
    }
}
