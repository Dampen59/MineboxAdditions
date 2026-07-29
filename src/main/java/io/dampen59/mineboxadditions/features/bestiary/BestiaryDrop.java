package io.dampen59.mineboxadditions.features.bestiary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BestiaryDrop {
    @JsonProperty("item_id")
    private String itemId;

    @JsonProperty("amount")
    private List<Integer> amount;

    @JsonProperty("chance")
    private double chance;

    public String getItemId() { return itemId; }
    public List<Integer> getAmount() { return amount; }
    public double getChance() { return chance; }
}
