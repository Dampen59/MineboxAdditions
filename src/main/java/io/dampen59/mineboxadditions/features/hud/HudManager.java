package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.huds.ShopHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.huds.haversack.HaversackManager;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.huds.MermaidHud;
import io.dampen59.mineboxadditions.features.hud.huds.WeatherHud;
import io.dampen59.mineboxadditions.features.hud.huds.itempickup.ItemPickupManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.EnumMap;
import java.util.Map;

public enum HudManager {
    INSTANCE;

    private final Map<Hud.Type, Hud> huds = new EnumMap<>(Hud.Type.class);

    public void init() {
        this.initHuds();
        new HaversackManager();
        new ItemPickupManager();
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        ShopHud shop = (ShopHud) getHud(Hud.Type.SHOP);
        if (shop.getState()) shop.draw(context);

        MermaidHud mermaid = (MermaidHud) getHud(Hud.Type.MERMAID_OFFER);
        if (mermaid.getState()) mermaid.draw(context);

        WeatherHud.RainHud rain = (WeatherHud.RainHud) getHud(Hud.Type.RAIN);
        if (rain.getState()) rain.draw(context);

        WeatherHud.StormHud storm = (WeatherHud.StormHud) getHud(Hud.Type.STORM);
        if (storm.getState()) storm.draw(context);

        WeatherHud.FullMoonHud fullMoon = (WeatherHud.FullMoonHud) getHud(Hud.Type.FULL_MOON);
        boolean isFullMoon = MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() == 0;
        if (isFullMoon && fullMoon.getState()) fullMoon.draw(context);
    }

    private void initHuds() {
        huds.put(Hud.Type.SHOP, new ShopHud());
        huds.put(Hud.Type.MERMAID_OFFER, new MermaidHud());
        huds.put(Hud.Type.RAIN, new WeatherHud.RainHud());
        huds.put(Hud.Type.STORM, new WeatherHud.StormHud());
        huds.put(Hud.Type.FULL_MOON, new WeatherHud.FullMoonHud());
        huds.put(Hud.Type.HAVERSACK_RATE, new HaversackHud.RateHud());
        huds.put(Hud.Type.HAVERSACK_FULL, new HaversackHud.FullHud());
        huds.put(Hud.Type.ITEM_PICKUP, new ItemPickupHud());
    }

    public Map<Hud.Type, Hud> getHuds() {
        return this.huds;
    }

    public Hud getHud(Hud.Type hud) {
        return this.huds.get(hud);
    }
}