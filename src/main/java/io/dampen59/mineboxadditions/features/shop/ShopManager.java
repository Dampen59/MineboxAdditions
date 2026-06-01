package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.huds.ShopHud;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.utils.models.Location;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.util.Arrays;

public class ShopManager {
    private static final MermaidItemOffer mermaid = new MermaidItemOffer();

    public static MermaidItemOffer getMermaid() {
        return mermaid;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ShopManager::tick);
        SocketManager.getSocket().on("S2CShopOfferEvent", ShopManager::update);
    }

    private static void tick(Minecraft client) {
        if (!Utils.isOnMinebox() || client.level == null) return;
        if (Utils.getTime() == LocalTime.MIDNIGHT) return;

        TextElement text = HudManager.INSTANCE.get(ShopHud.class)
                .getNamedElement("text", TextElement.class);

        boolean allClosed = true;
        for (Shop shop : Shop.values()) {
            if (shop.isOpen()) {
                allClosed = false;
                if (!shop.isEnabled()) continue;
                if (!shop.isAlerted()) {
                    showToast(shop);
                    shop.setAlerted(true);
                }
                text.setValue(Component.literal(shop.getName().getString() + (shop.getOffer() != null ? ": " + shop.getOffer().getString() : "")));
            } else {
                shop.reset();
            }
        }

        if (allClosed) {
            text.setValue(Component.translatable("mineboxadditions.shop.all_closed"));
        }
    }

    private static void update(Object[] args) {
        String shopName = (String) args[0];
        String itemName = (String) args[1];

        Shop shop = Arrays.stream(Shop.values())
                .filter(s -> s.name().equalsIgnoreCase(shopName))
                .findFirst()
                .orElse(null);

        if (shop != null && shop.getOffer() == null) {
            shop.setOffer(itemName);

            boolean shopEnabled = switch (shop.name().toLowerCase()) {
                case "mouse" -> HudsConfig.shop.mouse;
                case "bakery" -> HudsConfig.shop.bakery;
                case "buckstar" -> HudsConfig.shop.buckstar;
                case "sharkoffe" -> HudsConfig.shop.sharkoffe;
                default -> false;
            };

            if (!shopEnabled)
                return;

            showToast(shop, shop.getOffer());
        }
    }

    public static void reset() {
        for (Shop shop : Shop.values()) {
            shop.reset();
        }
    }

    public static void showToast(Shop shop) {
        showToast(shop, null);
    }
    public static void showToast(Shop shop, Component offer) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;

        Component text = offer != null
                ? Component.translatable("mineboxadditions." + shop.name().toLowerCase() + ".toast.offer", offer)
                : Component.translatable("mineboxadditions." + shop.name().toLowerCase() + ".toast");

        client.getToastManager().addToast(new MineboxToast(
                client.font,
                MineboxAdditions.id("textures/gui/toasts/" + shop.name().toLowerCase() + ".png"),
                shop.getName(),
                text
        ));
        client.player.playSound(SoundEvents.BELL_BLOCK, 1.0f, 1.0f);
    }

    public static class MermaidItemOffer {
        public int quantity = 0;
        public String itemTranslationKey = null;
        public String itemTranslationKeyArgs = null;

        public void set(int qty, String key, String args) {
            this.quantity = qty;
            this.itemTranslationKey = key;
            this.itemTranslationKeyArgs = args;
        }
    }
}
