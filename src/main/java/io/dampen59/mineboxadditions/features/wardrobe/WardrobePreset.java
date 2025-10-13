package io.dampen59.mineboxadditions.features.wardrobe;

import java.util.HashMap;
import java.util.Map;

public class WardrobePreset {
    public String name;
    public final Map<Integer, WardrobeItem> items = new HashMap<>();

    public WardrobePreset() {}
    public WardrobePreset(int id) {
        this.name = "Set " + (id + 1);
    }

    public static class WardrobeItem {
        public String id;
        public String uid;
        public String name;

        public WardrobeItem() {}
        public WardrobeItem(String id, String uid, String name) {
            this.id = id;
            this.uid = uid;
            this.name = name;
        }
    }
}
