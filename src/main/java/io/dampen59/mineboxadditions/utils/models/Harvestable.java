package io.dampen59.mineboxadditions.utils.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;

public class Harvestable {
    private String type;
    private String face;
    private String server;

    @JsonAdapter(LocationAdapter.class)
    private Location location;

    private static class Location {
        private int x, y, z;
    }

    private static class LocationAdapter implements JsonDeserializer<Location> {
        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String[] parts = json.getAsString().split(";");
            Location location = new Location();
            location.x = Integer.parseInt(parts[1]);
            location.y = Integer.parseInt(parts[2]);
            location.z = Integer.parseInt(parts[3]);
            return location;
        }
    }

    public int getX() {
        return location.x;
    }

    public int getY() {
        return location.y;
    }

    public int getZ() {
        return location.z;
    }

    public String getType() {
        return type;
    }

    public String getFace() {
        return face;
    }

    public String getServer() {
        return server;
    }
}