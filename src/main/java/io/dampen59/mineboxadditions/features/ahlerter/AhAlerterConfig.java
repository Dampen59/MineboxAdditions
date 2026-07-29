package io.dampen59.mineboxadditions.features.ahlerter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.entries.SerializableObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ConfigObject
public class AhAlerterConfig implements SerializableObject {
    private static final Gson GSON = new Gson();
    public List<AhAlert> alerts = new ArrayList<>();

    @Override
    public JsonElement save() {
        return GSON.toJsonTree(alerts);
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonArray()) return;
        Type type = new TypeToken<List<AhAlert>>() {}.getType();
        alerts = GSON.fromJson(json, type);
        if (alerts == null) alerts = new ArrayList<>();
    }

    public void addAlert(AhAlert alert) {
        alerts.add(alert);
    }

    public void removeAlert(int index) {
        if (index >= 0 && index < alerts.size()) {
            alerts.remove(index);
        }
    }

    public List<AhAlert> getAlertsForItem(String itemId) {
        return alerts.stream().filter(a -> itemId.equals(a.itemId)).toList();
    }

    public List<AhAlert> findMatches(String itemId, long pricePerUnit, Map<String, Integer> stats) {
        return alerts.stream().filter(a -> a.matches(itemId, pricePerUnit, stats)).toList();
    }
}
