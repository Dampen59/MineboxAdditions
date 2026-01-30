package io.dampen59.mineboxadditions.state;

import java.util.ArrayList;
import java.util.List;

public class WeatherState {
    private final List<Integer> rainTimestamps = new ArrayList<>();
    private final List<Integer> stormTimestamps = new ArrayList<>();

    public List<Integer> getRainTimestamps() { return rainTimestamps; }
    public List<Integer> getStormTimestamps() { return stormTimestamps; }

    public void addRainTimestamp(int timestamp) {
        if (!rainTimestamps.contains(timestamp)) rainTimestamps.add(timestamp);
    }

    public void addStormTimestamp(int timestamp) {
        if (!stormTimestamps.contains(timestamp)) stormTimestamps.add(timestamp);
    }

    public void clear() {
        rainTimestamps.clear();
        stormTimestamps.clear();
    }
}