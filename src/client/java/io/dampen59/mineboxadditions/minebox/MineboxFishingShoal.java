package io.dampen59.mineboxadditions.minebox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MineboxFishingShoal {
    private final List<FishingShoalFish> fishList;

    public MineboxFishingShoal() {
        this.fishList = new ArrayList<>();
    }

    public static class FishingShoalFish {
        private String name;
        private String shoal;
        private FishingShoalConditions conditions;
        @JsonProperty("time_range")
        private List<Integer> timeRange;
        private String texture;
        @JsonIgnore
        private Identifier textureId;

        public FishingShoalFish() {
        }

        public FishingShoalFish(String name, String shoal, FishingShoalConditions conditions, List<Integer> timeRange, String texture) {
            this.name = name;
            this.shoal = shoal;
            this.conditions = conditions;
            this.timeRange = timeRange;
            this.texture = texture;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public String getTexture() {
            return texture;
        }

        public Identifier getResource() {
            this.textureId = Identifier.of("mineboxadditions", "textures/fish/" + name + ".png");
            return textureId;
        }

        public void setResource(Identifier resource) {
            this.textureId = resource;
            System.out.println(this.name + " resource set to: " + resource);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShoal() {
            return shoal;
        }

        public void setShoal(String shoal) {
            this.shoal = shoal;
        }

        public FishingShoalConditions getConditions() {
            return conditions;
        }

        public void setConditions(FishingShoalConditions conditions) {
            this.conditions = conditions;
        }

        @JsonProperty("time_range")
        public List<Integer> getTimeRange() {
            return timeRange;
        }

        @JsonProperty("time_range")
        public void setTimeRange(List<Integer> timeRange) {
            this.timeRange = timeRange;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }

        @Override
        public String toString() {
            return "FishingShoalFish{" +
                    "name='" + name + '\'' +
                    ", shoal='" + shoal + '\'' +
                    ", conditions=" + conditions +
                    ", timeRange=" + timeRange +
                    '}';
        }
    }

    public static class FishingShoalConditions {
        private Boolean rain;
        private Boolean storm;
        @JsonProperty("full_moon")
        private Boolean fullMoon;

        public FishingShoalConditions() {
        }

        public FishingShoalConditions(Boolean rain, Boolean storm, Boolean fullMoon) {
            this.rain = rain;
            this.storm = storm;
            this.fullMoon = fullMoon;
        }

        public Boolean getRain() {
            return rain;
        }

        public void setRain(Boolean rain) {
            this.rain = rain;
        }

        public Boolean getStorm() {
            return storm;
        }

        public void setStorm(Boolean storm) {
            this.storm = storm;
        }

        @JsonProperty("full_moon")
        public Boolean getFullMoon() {
            return fullMoon;
        }

        @JsonProperty("full_moon")
        public void setFullMoon(Boolean fullMoon) {
            this.fullMoon = fullMoon;
        }

        @Override
        public String toString() {
            return "FishingShoalConditions{" +
                    "rain=" + rain +
                    ", storm=" + storm +
                    ", fullMoon=" + fullMoon +
                    '}';
        }
    }

    public void addFish(FishingShoalFish fish) {
        fishList.add(fish);
    }

    public List<FishingShoalFish> getFishList() {
        return fishList;
    }
}