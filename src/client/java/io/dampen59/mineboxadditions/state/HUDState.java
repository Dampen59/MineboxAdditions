package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.hud.ItemPickupHud;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.Map;

public class HUDState {
    private final Map<Hud.Type, Hud> huds = new EnumMap<>(Hud.Type.class);

    public HUDState() {
        huds.put(Hud.Type.RAIN, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().rainHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().rainHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().rainHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().rainHudY = y,
                "rain", Text.of("Next Rain: 00:00:00")));

        huds.put(Hud.Type.STORM, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().stormHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().stormHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().stormHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().stormHudY = y,
                "storm", Text.of("Next Storm: 00:00:00")));

        huds.put(Hud.Type.SHOP, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopHudY = y,
                "shop", Text.of("Shop Name: Shop Offer")));

        huds.put(Hud.Type.FULL_MOON, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().fullMoonHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().fullMoonHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().fullMoonHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().fullMoonHudY = y,
                "full_moon"));

        huds.put(Hud.Type.HAVERSACK_RATE, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haverSackFillRateX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haverSackFillRateY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haverSackFillRateX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haverSackFillRateY = y,
                "haversack", Text.of("Haversack Fill Rate: 0.0/s")));

        huds.put(Hud.Type.HAVERSACK_FULL, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haversackFullInX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haversackFullInY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haversackFullInX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().haversackFullInY = y,
                "haversack", Text.of("Haversack Full In: 00:00:00")));

        huds.put(Hud.Type.MERMAID_OFFER, new Hud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().mermaidRequestHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().getMermaidRequestHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().mermaidRequestHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().getMermaidRequestHudY = y,
                "mermaid", Text.of("Mermaid Request: 1x Bedrock")));

        huds.put(Hud.Type.ITEM_PICKUP, new ItemPickupHud(
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemPickupHudX,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemPickupHudY,
                x -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemPickupHudX = x,
                y -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().itemPickupHudY = y,
                new ItemStack(Items.DIAMOND)));
    }

    public Map<Hud.Type, Hud> getHuds() {
        return this.huds;
    }

    public Hud getHud(Hud.Type hud) {
        return this.huds.get(hud);
    }
}