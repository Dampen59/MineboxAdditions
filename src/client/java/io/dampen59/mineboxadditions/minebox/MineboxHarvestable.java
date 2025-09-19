package io.dampen59.mineboxadditions.minebox;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MineboxHarvestable {
    @JsonProperty("category")
    private String category;

    @JsonProperty("name")
    private String name;

    @JsonProperty("coordinates")
    private List<Double> coordinates;

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }
}