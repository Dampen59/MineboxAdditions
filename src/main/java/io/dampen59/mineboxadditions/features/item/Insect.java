package io.dampen59.mineboxadditions.features.item;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Insect {

    public static class TimeRange {
        @JsonProperty("from") private int from;
        @JsonProperty("to")   private int to;

        public TimeRange() {}

        public int getFrom() { return from; }
        public int getTo()   { return to; }

        public boolean isActive(int hour) {
            if (from == 0 && to == 0) return true;
            if (to == 0) return hour >= from;
            if (from < to) return hour >= from && hour < to;
            return hour >= from || hour < to;
        }

        @Override
        public String toString() {
            if (from == 0 && to == 0) return "All day";
            return fmt(from) + " – " + fmt(to);
        }

        private static String fmt(int h) { return h == 0 ? "00h" : h + "h"; }
    }

    public enum Weather {
        CLEAR, RAIN, ANY;

        public boolean isActive(boolean raining) {
            return switch (this) {
                case CLEAR -> !raining;
                case RAIN  -> raining;
                case ANY   -> true;
            };
        }

        public String display() {
            return switch (this) {
                case CLEAR -> "Clear";
                case RAIN  -> "Rain";
                case ANY   -> "Clear / Rain";
            };
        }
    }

    public static class Location {
        @JsonProperty("zone")    private String zone;
        @JsonProperty("subarea") private String subarea;
        public Location() {}
        public String getZone()    { return zone; }
        public String getSubarea() { return subarea; }
    }

    @JsonProperty("id")            private String id;
    @JsonProperty("time_ranges")   private List<TimeRange> timeRanges;
    @JsonProperty("weather")       private Weather weather;
    @JsonProperty("requires_moon") private boolean requiresMoon;
    @JsonProperty("locations")     private List<Location> locations;

    public Insect() {}

    public String getId()                  { return id; }
    public List<TimeRange> getTimeRanges() { return timeRanges; }
    public Weather getWeather()            { return weather; }
    public boolean requiresMoon()          { return requiresMoon; }
    public List<Location> getLocations()   { return locations; }

    public boolean canSpawn(int hour, boolean raining, int moonPhase) {
        if (!weather.isActive(raining)) return false;
        if (requiresMoon && moonPhase != 0 && moonPhase != 4) return false;
        return timeRanges.stream().anyMatch(r -> r.isActive(hour));
    }
}
