package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.features.hud.huds.*;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackManager;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

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

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("mineboxadditions", "main_huds"),
            (context, delta) -> {
                Minecraft client = Minecraft.getInstance();
                if (client == null || client.player == null || client.gui.hud.isHidden()) return;
                for (Hud hud : INSTANCE.getAll()) {
                    if (hud instanceof ItemPickupHud) continue;
                    if (hud instanceof HaversackHud.RateHud) continue;
                    if (hud instanceof HaversackHud.FullHud) continue;
                    if (hud.getState() && hud.shouldRender()) hud.draw(context);
                }
            }
        );
    }

    private void initHuds() {
        add(new IslandHud());
        add(new TimeHud());
        add(new VoteHud());
        add(new BossBarHuds.KeyFragmentHud());
        add(new BossBarHuds.StatsHud());
        add(new BossBarHuds.FreeItemHud());
        add(new ShopHud());
        add(new MermaidHud());
        add(new WeatherHud.RainHud());
        add(new WeatherHud.StormHud());
        add(new WeatherHud.MoonHud());
        add(new HaversackHud.RateHud());
        add(new HaversackHud.FullHud());
        add(new ItemPickupHud());
        add(new InsectsHud());
    }
}
