package io.dampen59.mineboxadditions.config.other;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.entries.SerializableObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@ConfigObject
public class HarvestablesSettings implements SerializableObject {
    private static final Gson GSON = new Gson();
    public Map<String, Harvestable> harvestables = new HashMap<>();

    public static class Harvestable {
        public Map<String, Boolean> categories = new HashMap<>();
        public Map<String, Map<String, Boolean>> items = new HashMap<>();
        public Map<String, Map<String, Integer>> colors = new HashMap<>();
    }

    @Override
    public JsonElement save() {
        return GSON.toJsonTree(this.harvestables);
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonObject()) return;
        Type type = new TypeToken<Map<String, Harvestable>>() {}.getType();
        this.harvestables = GSON.fromJson(json, type);
    }
}