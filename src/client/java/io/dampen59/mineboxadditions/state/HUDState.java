package io.dampen59.mineboxadditions.state;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.hud.ItemPickupHud;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.Map;

public class HUDState {
    private final Map<Hud.Type, Hud> huds = new EnumMap<>(Hud.Type.class);

    public HUDState() {
        huds.put(Hud.Type.RAIN, new Hud(
                () -> MineboxAdditionConfig.get().rainHudX,
                () -> MineboxAdditionConfig.get().rainHudY,
                x -> MineboxAdditionConfig.get().rainHudX = x,
                y -> MineboxAdditionConfig.get().rainHudY = y,
                "rain", Text.of("Next Rain: 00:00:00")));

        huds.put(Hud.Type.STORM, new Hud(
                () -> MineboxAdditionConfig.get().stormHudX,
                () -> MineboxAdditionConfig.get().stormHudY,
                x -> MineboxAdditionConfig.get().stormHudX = x,
                y -> MineboxAdditionConfig.get().stormHudY = y,
                "storm", Text.of("Next Storm: 00:00:00")));

        huds.put(Hud.Type.SHOP, new Hud(
                () -> MineboxAdditionConfig.get().shopHudX,
                () -> MineboxAdditionConfig.get().shopHudY,
                x -> MineboxAdditionConfig.get().shopHudX = x,
                y -> MineboxAdditionConfig.get().shopHudY = y,
                "shop", Text.of("Shop Name: Shop Offer")));

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

        huds.put(Hud.Type.MERMAID_OFFER, new Hud(
                () -> MineboxAdditionConfig.get().mermaidRequestHudX,
                () -> MineboxAdditionConfig.get().getMermaidRequestHudY,
                x -> MineboxAdditionConfig.get().mermaidRequestHudX = x,
                y -> MineboxAdditionConfig.get().getMermaidRequestHudY = y,
                "mermaid", Text.of("1x Bedrock")));

        huds.put(Hud.Type.ITEM_PICKUP, new ItemPickupHud(
                () -> MineboxAdditionConfig.get().itemPickupHudX,
                () -> MineboxAdditionConfig.get().itemPickupHudY,
                x -> MineboxAdditionConfig.get().itemPickupHudX = x,
                y -> MineboxAdditionConfig.get().itemPickupHudY = y,
                new ItemStack(Items.DIAMOND)));
    }

    public Map<Hud.Type, Hud> getHuds() {
        return this.huds;
    }

    public Hud getHud(Hud.Type hud) {
        return this.huds.get(hud);
    }
}