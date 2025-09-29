package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.MineboxAdditions;
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
                () -> MineboxAdditions.INSTANCE.config.rainHudX,
                () -> MineboxAdditions.INSTANCE.config.rainHudY,
                x -> MineboxAdditions.INSTANCE.config.rainHudX = x,
                y -> MineboxAdditions.INSTANCE.config.rainHudY = y,
                "rain", Text.of("Next Rain: 00:00:00")));

        huds.put(Hud.Type.STORM, new Hud(
                () -> MineboxAdditions.INSTANCE.config.stormHudX,
                () -> MineboxAdditions.INSTANCE.config.stormHudY,
                x -> MineboxAdditions.INSTANCE.config.stormHudX = x,
                y -> MineboxAdditions.INSTANCE.config.stormHudY = y,
                "storm", Text.of("Next Storm: 00:00:00")));

        huds.put(Hud.Type.SHOP, new Hud(
                () -> MineboxAdditions.INSTANCE.config.shopHudX,
                () -> MineboxAdditions.INSTANCE.config.shopHudY,
                x -> MineboxAdditions.INSTANCE.config.shopHudX = x,
                y -> MineboxAdditions.INSTANCE.config.shopHudY = y,
                "shop", Text.of("Shop Name: Shop Offer")));

        huds.put(Hud.Type.FULL_MOON, new Hud(
                () -> MineboxAdditions.INSTANCE.config.fullMoonHudX,
                () -> MineboxAdditions.INSTANCE.config.fullMoonHudY,
                x -> MineboxAdditions.INSTANCE.config.fullMoonHudX = x,
                y -> MineboxAdditions.INSTANCE.config.fullMoonHudY = y,
                "full_moon"));

        huds.put(Hud.Type.HAVERSACK_RATE, new Hud(
                () -> MineboxAdditions.INSTANCE.config.haverSackFillRateX,
                () -> MineboxAdditions.INSTANCE.config.haverSackFillRateY,
                x -> MineboxAdditions.INSTANCE.config.haverSackFillRateX = x,
                y -> MineboxAdditions.INSTANCE.config.haverSackFillRateY = y,
                "haversack", Text.of("Fill Rate: 0.0/s")));

        huds.put(Hud.Type.HAVERSACK_FULL, new Hud(
                () -> MineboxAdditions.INSTANCE.config.haversackFullInX,
                () -> MineboxAdditions.INSTANCE.config.haversackFullInY,
                x -> MineboxAdditions.INSTANCE.config.haversackFullInX = x,
                y -> MineboxAdditions.INSTANCE.config.haversackFullInY = y,
                "haversack", Text.of("Full In: 00:00:00")));

        huds.put(Hud.Type.MERMAID_OFFER, new Hud(
                () -> MineboxAdditions.INSTANCE.config.mermaidRequestHudX,
                () -> MineboxAdditions.INSTANCE.config.getMermaidRequestHudY,
                x -> MineboxAdditions.INSTANCE.config.mermaidRequestHudX = x,
                y -> MineboxAdditions.INSTANCE.config.getMermaidRequestHudY = y,
                "mermaid", Text.of("1x Bedrock")));

        huds.put(Hud.Type.ITEM_PICKUP, new ItemPickupHud(
                () -> MineboxAdditions.INSTANCE.config.itemPickupHudX,
                () -> MineboxAdditions.INSTANCE.config.itemPickupHudY,
                x -> MineboxAdditions.INSTANCE.config.itemPickupHudX = x,
                y -> MineboxAdditions.INSTANCE.config.itemPickupHudY = y,
                new ItemStack(Items.DIAMOND)));
    }

    public Map<Hud.Type, Hud> getHuds() {
        return this.huds;
    }

    public Hud getHud(Hud.Type hud) {
        return this.huds.get(hud);
    }
}