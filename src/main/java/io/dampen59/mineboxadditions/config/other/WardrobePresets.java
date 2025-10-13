package io.dampen59.mineboxadditions.config.other;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.entries.SerializableObject;
import io.dampen59.mineboxadditions.features.wardrobe.WardrobePreset;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@ConfigObject
public class WardrobePresets implements SerializableObject {
    private static final Gson GSON = new Gson();
    public Map<Integer, WardrobePreset> presets = new HashMap<>();

    public WardrobePresets() {
        for (int i = 0; i < 4; i++) presets.put(i, new WardrobePreset(i));
    }

    public WardrobePreset getPreset(int presetId) {
        return presets.getOrDefault(presetId, new WardrobePreset(presetId));
    }

    public void clearPreset(int presetId) {
        if (presetId >= 0 && presetId < 4) {
            WardrobePreset preset = presets.get(presetId);
            if (preset != null) preset.items.clear();
            else presets.put(presetId, new WardrobePreset(presetId));
        }
    }

    public void setPresetItem(int presetId, int slotId, WardrobePreset.WardrobeItem item) {
        if (presetId >= 0 && presetId < 4) {
            presets.get(presetId).items.put(slotId, item);
        }
    }

    @Override
    public JsonElement save() {
        return GSON.toJsonTree(presets);
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonObject()) return;
        Type type = new TypeToken<Map<Integer, WardrobePreset>>() {}.getType();
        this.presets = GSON.fromJson(json, type);
    }
}
