package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.huds.*;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackManager;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.Collection;
import java.util.EnumMap;
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
        HudRenderCallback.EVENT.register(this::render);
    }

    private void initHuds() {
        add(new IslandHud());
        add(new TimeHud());
        add(new VoteHud());
        add(new ShopHud());
        add(new MermaidHud());
        add(new WeatherHud.RainHud());
        add(new WeatherHud.StormHud());
        add(new WeatherHud.FullMoonHud());
        add(new HaversackHud.RateHud());
        add(new HaversackHud.FullHud());
        add(new ItemPickupHud());
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        ShopHud shop = get(ShopHud.class);
        if (shop.getState()) shop.draw(context);

        MermaidHud mermaid = get(MermaidHud.class);
        if (mermaid.getState()) mermaid.draw(context);

        WeatherHud.RainHud rain = get(WeatherHud.RainHud.class);
        if (rain.getState()) rain.draw(context);

        WeatherHud.StormHud storm = get(WeatherHud.StormHud.class);
        if (storm.getState()) storm.draw(context);

        WeatherHud.FullMoonHud fullMoon = get(WeatherHud.FullMoonHud.class);
        boolean isFullMoon = MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() == 0;
        if (isFullMoon && fullMoon.getState()) fullMoon.draw(context);
    }
}