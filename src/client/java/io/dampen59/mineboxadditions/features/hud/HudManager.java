package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.haversack.HaversackHud;
import io.dampen59.mineboxadditions.features.hud.haversack.HaversackManager;
import io.dampen59.mineboxadditions.features.hud.itempickup.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.other.MermaidHud;
import io.dampen59.mineboxadditions.features.hud.other.WeatherHud;
import io.dampen59.mineboxadditions.features.hud.itempickup.ItemPickupManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

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

        boolean isFullMoon = MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() == 0;
        if (isFullMoon && HudsConfig.fullMoon) {
            HudManager.INSTANCE.getHud(Hud.Type.FULL_MOON).draw(context);
        }

        WeatherHud.RainHud.render(context, client);
        WeatherHud.StormHud.render(context, client);
        MermaidHud.render(context);
    }

    private void initHuds() {
        huds.put(Hud.Type.SHOP, new Hud(
                () -> HudsConfig.shop.enabled,
                s -> HudsConfig.shop.enabled = s,
                () -> HudPositions.shop.x,
                () -> HudPositions.shop.y,
                x -> HudPositions.shop.x = x,
                y -> HudPositions.shop.y = y,
                "shop", Text.of("Shop Name: Shop Offer")));

        huds.put(Hud.Type.MERMAID_OFFER, new MermaidHud());
        huds.put(Hud.Type.RAIN, new WeatherHud.RainHud());
        huds.put(Hud.Type.STORM, new WeatherHud.StormHud());
        huds.put(Hud.Type.FULL_MOON, new Hud(
                () -> HudsConfig.fullMoon,
                s -> HudsConfig.fullMoon = s,
                () -> HudPositions.fullMoon.x,
                () -> HudPositions.fullMoon.y,
                x -> HudPositions.fullMoon.x = x,
                y -> HudPositions.fullMoon.y = y,
                "full_moon"));

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