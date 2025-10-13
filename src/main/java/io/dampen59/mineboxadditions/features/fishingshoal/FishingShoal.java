package io.dampen59.mineboxadditions.features.fishingshoal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dampen59.mineboxadditions.MineboxAdditions;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class FishingShoal {
    private final List<Item> items = new ArrayList<>();

    public static class Item {
        private String name;
        private String shoal;
        private Conditions conditions;
        @JsonProperty("time_range")
        private List<Integer> timeRange;
        private String texture;
        @JsonIgnore
        private Identifier textureId;

        public Item() {}
        public Item(String name, String shoal, Conditions conditions, List<Integer> timeRange, String texture) {
            this.name = name;
            this.shoal = shoal;
            this.conditions = conditions;
            this.timeRange = timeRange;
            this.texture = texture;
        }

        public String getName() {
            return name;
        }

        public String getShoal() {
            return shoal;
        }

        public Conditions getConditions() {
            return conditions;
        }

        public List<Integer> getTimeRange() {
            return timeRange;
        }

        public String getTexture() {
            return texture;
        }

        public Identifier getResource() {
            return Identifier.of(MineboxAdditions.NAMESPACE, "textures/fish/" + name + ".png");
        }

        public void setResource(Identifier resource) {
            this.textureId = resource;
            System.out.println(this.name + " resource set to: " + resource);
        }

        @Override
        public String toString() {
            return "FishingShoal.Item{" +
                    "name='" + name + '\'' +
                    ", shoal='" + shoal + '\'' +
                    ", conditions=" + conditions +
                    ", timeRange=" + timeRange +
                    '}';
        }
    }

    public static class Conditions {
        private Boolean rain;
        private Boolean storm;
        @JsonProperty("full_moon")
        private Boolean fullMoon;

        public Conditions() {}
        public Conditions(Boolean rain, Boolean storm, Boolean fullMoon) {
            this.rain = rain;
            this.storm = storm;
            this.fullMoon = fullMoon;
        }

        public Boolean getRain() {
            return rain;
        }

        public Boolean getStorm() {
            return storm;
        }

        @JsonProperty("full_moon")
        public Boolean getFullMoon() {
            return fullMoon;
        }

        @Override
        public String toString() {
            return "FishingShoal.Conditions{" +
                    "rain=" + rain +
                    ", storm=" + storm +
                    ", fullMoon=" + fullMoon +
                    '}';
        }
    }
}