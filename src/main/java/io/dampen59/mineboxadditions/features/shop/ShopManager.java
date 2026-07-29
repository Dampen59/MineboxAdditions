package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.notifications.NotificationsConfig;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.huds.ShopHud;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ShopManager {
    private static final MermaidItemOffer mermaid = new MermaidItemOffer();
    private static boolean hudFrozen = false;

    public static boolean toggleHudFreeze() {
        hudFrozen = !hudFrozen;
        return hudFrozen;
    }

    public static MermaidItemOffer getMermaid() {
        return mermaid;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ShopManager::tick);
        SocketManager.getSocket().on("S2CShopOfferEvent", ShopManager::update);
    }

    private static void tick(Minecraft client) {
        if (hudFrozen) return;
        if (!Utils.isOnMinebox() || client.level == null) return;
        if (!Utils.isTimeKnown()) return;

        TextElement text = HudManager.INSTANCE.get(ShopHud.class)
                .getNamedElement("text", TextElement.class);

        List<String> openLabels = new ArrayList<>();

        for (Shop shop : Shop.values()) {
            if (shop.isOpen()) {
                if (shop.isEnabled()) {
                    if (!shop.isAlerted()) {
                        showToast(shop);
                        shop.setAlerted(true);
                    }
                    String label = shop.getName().getString()
                            + (shop.getOffer() != null ? ": " + shop.getOffer().getString() : "");
                    openLabels.add(label);
                }
            } else {
                shop.reset();
            }
        }

        if (openLabels.isEmpty()) {
            text.setValue(Component.translatable("mineboxadditions.shop.all_closed"));
        } else {
            text.setLines(openLabels.stream().map(Component::literal).collect(Collectors.toList()));
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
                case "mouse"           -> NotificationsConfig.shop.mouseToast          || NotificationsConfig.shop.mouseBell;
                case "bakery"          -> NotificationsConfig.shop.bakeryToast         || NotificationsConfig.shop.bakeryBell;
                case "buckstar"        -> NotificationsConfig.shop.buckstarToast        || NotificationsConfig.shop.buckstarBell;
                case "sharkoffe"       -> NotificationsConfig.shop.sharkoffeToast       || NotificationsConfig.shop.sharkoffeBell;
                case "reggae_dealer"   -> NotificationsConfig.shop.reggaeDealerToast    || NotificationsConfig.shop.reggaeDealerBell;
                case "paintings_seller" -> NotificationsConfig.shop.paintingsSellerToast || NotificationsConfig.shop.paintingsSellerBell;
                case "sushi_seller"     -> NotificationsConfig.shop.sushiSellerToast     || NotificationsConfig.shop.sushiSellerBell;
                default -> false;
            };

            if (!shopEnabled) return;

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

        boolean toastEnabled = switch (shop.name().toLowerCase()) {
            case "mouse"            -> NotificationsConfig.shop.mouseToast;
            case "bakery"           -> NotificationsConfig.shop.bakeryToast;
            case "buckstar"         -> NotificationsConfig.shop.buckstarToast;
            case "sharkoffe"        -> NotificationsConfig.shop.sharkoffeToast;
            case "reggae_dealer"    -> NotificationsConfig.shop.reggaeDealerToast;
            case "paintings_seller" -> NotificationsConfig.shop.paintingsSellerToast;
            case "sushi_seller"     -> NotificationsConfig.shop.sushiSellerToast;
            default -> false;
        };

        boolean bellEnabled = switch (shop.name().toLowerCase()) {
            case "mouse"            -> NotificationsConfig.shop.mouseBell;
            case "bakery"           -> NotificationsConfig.shop.bakeryBell;
            case "buckstar"         -> NotificationsConfig.shop.buckstarBell;
            case "sharkoffe"        -> NotificationsConfig.shop.sharkoffeBell;
            case "reggae_dealer"    -> NotificationsConfig.shop.reggaeDealerBell;
            case "paintings_seller" -> NotificationsConfig.shop.paintingsSellerBell;
            case "sushi_seller"     -> NotificationsConfig.shop.sushiSellerBell;
            default -> false;
        };

        if (toastEnabled) {
            Component text = offer != null
                    ? Component.translatable("mineboxadditions." + shop.name().toLowerCase() + ".toast.offer", offer)
                    : Component.translatable("mineboxadditions." + shop.name().toLowerCase() + ".toast");

            client.gui.toastManager().addToast(new MineboxToast(
                    client.font,
                    MineboxAdditions.id("textures/gui/toasts/" + shop.name().toLowerCase() + ".png"),
                    shop.getName(),
                    text
            ));
        }

        if (bellEnabled) {
            client.player.playSound(SoundEvents.BELL_BLOCK, 1.0f, 1.0f);
        }
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
