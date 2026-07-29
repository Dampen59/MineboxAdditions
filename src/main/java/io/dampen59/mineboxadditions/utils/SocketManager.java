package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.features.ahlerter.AhAlert;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.utils.security.SessionConnector;
import net.minecraft.sounds.SoundEvents;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class SocketManager {
    private static Socket socket;
    private static final int protocol = 13;
    private static volatile String pendingSessionToken = null;
    @NotNull
    public static Socket getSocket() {
        if (socket == null) init();
        return socket;
    }

    public static void connectWithSessionToken(String sessionToken) {
        pendingSessionToken = sessionToken;
        getSocket().connect();
    }

    public static void init() {
        socket = IO.socket(URI.create("https://mineboxadditions.bartier.me"), IO.Options.builder().build());

        socket.on(Socket.EVENT_CONNECT, args -> {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.player != null) {
                String playerName = client.player.getName().getString();
                String playerUuid = client.player.getUUID().toString();
                String playerLang = client.getLanguageManager().getSelected();
                socket.emit("C2SHelloConnectMessage", playerUuid, playerName, playerLang, protocol, pendingSessionToken != null ? pendingSessionToken : "");
                ApiUtils.fetchAll(MineboxAdditions.INSTANCE.state);
            }
        });

        socket.on("S2CProtocolMismatch", args -> Utils.showToastNotification(
                Component.translatable("mineboxadditions.strings.update.title").getString(),
                Component.translatable("mineboxadditions.strings.update.content").getString()));

        socket.on("S2CWeatherData", args -> {
            String weather = (String) args[0];
            Integer timestamp = Integer.parseInt(args[1].toString());

            switch (weather) {
                case "RAIN" -> MineboxAdditions.INSTANCE.state.getWeatherState().addRainTimestamp(timestamp);
                case "STORM" -> {
                    MineboxAdditions.INSTANCE.state.getWeatherState().addRainTimestamp(timestamp); // Storms also equals rain :)
                    MineboxAdditions.INSTANCE.state.getWeatherState().addStormTimestamp(timestamp);
                }
                default -> System.out.println("Received unknown weather data : " + weather);
            }
        });

        socket.on("S2ClearWeatherData", args -> {
            MineboxAdditions.INSTANCE.state.getWeatherState().clear();
        });

        socket.on("S2CMotd", args -> {
            String message = (String) args[0];
            Utils.displayChatInfoMessage("[MineboxAdditions MOTD] " + message);
        });

        socket.on("S2CServerMessage", args -> {
            String type = (String) args[0];
            Boolean isRaw = (Boolean) args[1];
            String data = (String) args[2];

            String message = isRaw ? data : Component.translatable(data).getString();
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                switch (type) {
                    case "SUCCESS" -> Utils.displayChatSuccessMessage(message);
                    case "ERROR"   -> Utils.displayChatErrorMessage(message);
                    default        -> Utils.displayChatInfoMessage(message);
                }
            });
        });

        socket.on("S2CMermaidRequest", args -> {
            int itemQuantity = (int) args[0];
            String itemTranslationKey = (String) args[1];
            String itemTranslationKeyArgs = (args[2] instanceof String) ? (String) args[2] : null;
            ShopManager.getMermaid().set(itemQuantity, itemTranslationKey, itemTranslationKeyArgs);
        });

        socket.on("S2CInvalidSession", args -> {
            SessionConnector.fetch(SocketManager::connectWithSessionToken);
        });

        socket.on("S2CMineboxApiUnauthorized", args -> {
            Utils.displayChatErrorMessage(Component
                    .translatable("mineboxadditions.strings.errors.unauthorized-api").getString());
        });

        socket.on("S2CMissingMuseumItems", args -> {
            List<String> itemIds = new ArrayList<>();
            Object payload = args[0];
            JSONArray arr = (JSONArray) payload;
            for (int i = 0; i < arr.length(); i++) {
                String id = arr.optString(i, null);
                if (id != null && !id.isEmpty()) {
                    itemIds.add(id);
                }
            }
            MineboxAdditions.INSTANCE.state.setMissingMuseumItemIds(itemIds);
        });

        socket.on("S2CAuctionsList", args -> {
            if (Config.ahAlerts.alerts.isEmpty()) return;
            JSONArray arr = (JSONArray) args[0];
            List<String> matchedItemIds = new ArrayList<>();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject entry = arr.optJSONObject(i);
                if (entry == null) continue;
                String rawId = entry.optString("item_id", null);
                if (rawId == null) continue;
                String itemId = rawId.startsWith("mbi-") ? rawId.substring(4) : rawId;
                if (itemId == null) continue;
                long price = entry.optLong("price_per_unit", 0);

                Map<String, Integer> stats = new HashMap<>();
                JSONObject statsJson = entry.optJSONObject("stats");
                if (statsJson != null) {
                    Iterator<String> keys = statsJson.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        stats.put("mbx_stats_" + k.toLowerCase(), statsJson.optInt(k, 0));
                    }
                }

                List<AhAlert> matches = Config.ahAlerts.findMatches(itemId, price, stats);
                if (!matches.isEmpty() && !matchedItemIds.contains(itemId)) {
                    matchedItemIds.add(itemId);
                }
            }

            if (!matchedItemIds.isEmpty()) {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> {
                    if (client.player == null) return;
                    client.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
                    MineboxItem first = MineboxAdditions.INSTANCE.state.getItemById(matchedItemIds.get(0));
                    Component title = Component.literal("AH-Lert!")
                            .withStyle(s -> s.withBold(true).withColor(0xFFAA00));
                    Component subtitle = MineboxItem.getDisplayName(first != null ? first : null) != null && first != null
                            ? MineboxItem.getDisplayName(first)
                            : Component.literal(matchedItemIds.get(0));
                    if (matchedItemIds.size() > 1)
                        subtitle = subtitle.copy().append(Component.literal(" +" + (matchedItemIds.size() - 1) + " more")
                                .withStyle(s -> s.withColor(0xAAAAAA)));
                    client.gui.hud.setTitle(title);
                    client.gui.hud.setSubtitle(subtitle);
                    client.gui.hud.setTimes(10, 100, 20);
                });
            }
        });

    }
}
