package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.hud.huds.ItemPickupHud;
import io.dampen59.mineboxadditions.features.hud.huds.MermaidHud;
import io.dampen59.mineboxadditions.features.hud.huds.WeatherHud;
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
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;
        MineboxAdditionConfig config = MineboxAdditionConfig.get();

        boolean isFullMoon = MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() == 0;
        if (isFullMoon && config.displaySettings.displayFullMoon) {
            HudManager.INSTANCE.getHud(Hud.Type.FULL_MOON).draw(context);
        }

        WeatherHud.RainHud.render(context, client);
        WeatherHud.StormHud.render(context, client);
        MermaidHud.render(context);
    }

    private void initHuds() {
        huds.put(Hud.Type.SHOP, new Hud(
                () -> MineboxAdditionConfig.get().shopHudX,
                () -> MineboxAdditionConfig.get().shopHudY,
                x -> MineboxAdditionConfig.get().shopHudX = x,
                y -> MineboxAdditionConfig.get().shopHudY = y,
                "shop", Text.of("Shop Name: Shop Offer")));

        huds.put(Hud.Type.MERMAID_OFFER, new MermaidHud());
        huds.put(Hud.Type.RAIN, new WeatherHud.RainHud());
        huds.put(Hud.Type.STORM, new WeatherHud.StormHud());
        huds.put(Hud.Type.FULL_MOON, new Hud(
                () -> MineboxAdditionConfig.get().fullMoonHudX,
                () -> MineboxAdditionConfig.get().fullMoonHudY,
                x -> MineboxAdditionConfig.get().fullMoonHudX = x,
                y -> MineboxAdditionConfig.get().fullMoonHudY = y,
                "full_moon"));

        huds.put(Hud.Type.HAVERSACK_RATE, new Hud(
                () -> MineboxAdditionConfig.get().haverSackFillRateX,
                () -> MineboxAdditionConfig.get().haverSackFillRateY,
                x -> MineboxAdditionConfig.get().haverSackFillRateX = x,
                y -> MineboxAdditionConfig.get().haverSackFillRateY = y,
                "haversack", Text.of("Fill Rate: 0.0/s")));

        huds.put(Hud.Type.HAVERSACK_FULL, new Hud(
                () -> MineboxAdditionConfig.get().haversackFullInX,
                () -> MineboxAdditionConfig.get().haversackFullInY,
                x -> MineboxAdditionConfig.get().haversackFullInX = x,
                y -> MineboxAdditionConfig.get().haversackFullInY = y,
                "haversack", Text.of("Full In: 00:00:00")));

        huds.put(Hud.Type.ITEM_PICKUP, new ItemPickupHud());
    }

    public Map<Hud.Type, Hud> getHuds() {
        return this.huds;
    }

    public Hud getHud(Hud.Type hud) {
        return this.huds.get(hud);
    }
}