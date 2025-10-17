package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.features.hud.huds.*;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackManager;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public enum HudManager {
    INSTANCE;

    private final Map<Class<? extends Hud>, Hud> huds = new HashMap<>();

    public <T extends Hud> void add(T hud) {
        huds.putIfAbsent(hud.getClass(), hud);
    }

    public <T extends Hud> T get(Class<T> clazz) {
        return clazz.cast(huds.get(clazz));
    }

    public Collection<Hud> getAll() {
        return huds.values();
    }

    public void init() {
        this.initHuds();
        new HaversackManager();
        new ItemPickupManager();
    }

    private void initHuds() {
//        add(new IslandHud());
//        add(new TimeHud());
//        add(new VoteHud());
        add(new ShopHud());
        add(new MermaidHud());
        add(new WeatherHud.RainHud());
        add(new WeatherHud.StormHud());
        add(new WeatherHud.FullMoonHud());
        add(new HaversackHud.RateHud());
        add(new HaversackHud.FullHud());
        add(new ItemPickupHud());
    }
}