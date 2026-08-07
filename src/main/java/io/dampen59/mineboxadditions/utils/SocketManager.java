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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class SocketManager {
    private static Socket socket;
    private static final int PROTOCOL_MIN = 16;
    private static final int PROTOCOL_MAX = 16;
    private static final List<String> CAPABILITIES = List.of("weather", "museum", "ah_alerts", "trusted_events", "mermaid", "shop", "shiny");

    public enum ProtocolState { CONNECTED, NEGOTIATING, AUTHENTICATING, READY_TRUSTED, READY_UNTRUSTED }

    private static volatile ProtocolState state = ProtocolState.CONNECTED;
    private static volatile String currentChallenge = null;
    private static volatile boolean apiFetched = false;

    private static final AtomicBoolean sessionFetchInFlight = new AtomicBoolean(false);
    private static final AtomicInteger consecutiveSessionFailures = new AtomicInteger(0);
    private static final int BASE_BACKOFF_TICKS = 10;
    private static final int MAX_BACKOFF_TICKS = 3600;

    private static final Pattern TICKET_SHAPE = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");
    private static final Pattern LEGACY_TOKEN_SHAPE = Pattern.compile("^\\d+\\.[0-9a-f]{64}$");

    @NotNull
    public static Socket getSocket() {
        if (socket == null) init();
        return socket;
    }

    public static ProtocolState getState() {
        return state;
    }

    public static boolean isTrusted() {
        return state == ProtocolState.READY_TRUSTED;
    }

    private static void sendHello() {
        state = ProtocolState.NEGOTIATING;
        apiFetched = false;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            socket.disconnect();
            return;
        }

        try {
            JSONObject protocol = new JSONObject().put("min", PROTOCOL_MIN).put("max", PROTOCOL_MAX);
            JSONObject hello = new JSONObject()
                    .put("protocol", protocol)
                    .put("modVersion", Utils.getModVersion())
                    .put("locale", client.getLanguageManager().getSelected())
                    .put("capabilities", new JSONArray(CAPABILITIES));

            socket.emit("C2SHello", hello);
        } catch (org.json.JSONException e) {
            System.out.println("[MineboxAdditions] Failed to serialize C2SHello: " + e.getMessage());
        }
    }

    private static void authenticateWithChallenge(String challenge) {
        currentChallenge = challenge;
        state = ProtocolState.AUTHENTICATING;

        if (!sessionFetchInFlight.compareAndSet(false, true)) {
            return;
        }

        SessionConnector.fetch(challenge, token -> {
            sessionFetchInFlight.set(false);

            if (!challenge.equals(currentChallenge)) {
                authenticateWithChallenge(currentChallenge);
                return;
            }

            boolean looksLikeRealToken = token != null && (TICKET_SHAPE.matcher(token).matches() || LEGACY_TOKEN_SHAPE.matcher(token).matches());

            if (!looksLikeRealToken) {
                int failures = consecutiveSessionFailures.incrementAndGet();
                int backoffTicks = Math.min(BASE_BACKOFF_TICKS << Math.min(failures - 1, 6), MAX_BACKOFF_TICKS);
                int jitterTicks = ThreadLocalRandom.current().nextInt(backoffTicks / 4 + 1);
                Scheduler.INSTANCE.schedule(() -> authenticateWithChallenge(currentChallenge), backoffTicks + jitterTicks);
            } else {
                consecutiveSessionFailures.set(0);
                if (!socket.connected()) return;
                try {
                    socket.emit("C2SAuthenticate", new JSONObject().put("ticket", token));
                } catch (org.json.JSONException e) {
                    System.out.println("[MineboxAdditions] Failed to serialize C2SAuthenticate: " + e.getMessage());
                }
            }
        });
    }

    public static void init() {
        socket = IO.socket(URI.create("https://mineboxadditions.bartier.me"), IO.Options.builder().build());

        socket.on(Socket.EVENT_CONNECT, args -> {
            state = ProtocolState.CONNECTED;
            sendHello();
        });

        socket.on("S2CHello", args -> {
            JSONObject payload = (JSONObject) args[0];
            JSONObject protocol = payload.optJSONObject("protocol");
            boolean hasSelected = protocol != null && !protocol.isNull("selected");

            if (!hasSelected) {
                Utils.showToastNotification(
                        Component.translatable("mineboxadditions.strings.update.title").getString(),
                        Component.translatable("mineboxadditions.strings.update.content").getString());
                return; // goodbye, unsupported protocolVersion
            }

            if (!apiFetched) {
                apiFetched = true;
                ApiUtils.fetchAll(MineboxAdditions.INSTANCE.state);
            }

            JSONObject auth = payload.optJSONObject("auth");
            String challenge = auth != null ? auth.optString("challenge", null) : null;
            if (challenge != null) {
                authenticateWithChallenge(challenge);
            }
        });

        socket.on("S2CAuthChallenge", args -> {
            JSONObject payload = (JSONObject) args[0];
            String challenge = payload.optString("challenge", null);
            if (challenge != null) authenticateWithChallenge(challenge);
        });

        socket.on("S2CAuthResult", args -> {
            JSONObject payload = (JSONObject) args[0];
            boolean authenticated = payload.optBoolean("authenticated", false);
            boolean trusted = payload.optBoolean("trusted", false);

            if (authenticated && trusted) {
                state = ProtocolState.READY_TRUSTED;
                JSONObject player = payload.optJSONObject("player");
                System.out.println("[MineboxAdditions] Authenticated as " + (player != null ? player.optString("name") : "?") + " (trusted)");
            } else {
                state = ProtocolState.READY_UNTRUSTED;
                String error = payload.optString("error", "UNKNOWN");
                System.out.println("[MineboxAdditions] Authentication failed (" + error + ") — running untrusted");
            }
        });

        socket.on("S2CError", args -> {
            JSONObject payload = (JSONObject) args[0];
            System.out.println("[MineboxAdditions] S2CError " + payload.optString("code") + ": " + payload.optString("message"));
        });

        socket.on("S2CInitialState", args -> {
            JSONObject payload = (JSONObject) args[0];

            MineboxAdditions.INSTANCE.state.getWeatherState().clear();
            JSONObject weatherJson = payload.optJSONObject("weather");
            if (weatherJson != null) {
                applyTimestampArray(weatherJson.optJSONArray("rain"), false);
                applyTimestampArray(weatherJson.optJSONArray("storm"), true);
            }

            JSONObject mermaid = payload.optJSONObject("mermaid");
            if (mermaid != null && !mermaid.isNull("itemId")) {
                applyMermaidUpdate(mermaid);
            }
        });

        socket.on("S2CWeatherUpdate", args -> {
            JSONObject entry = (JSONObject) args[0];
            applyWeatherEntry(entry.optString("type"), entry.optLong("timestamp"));
        });

        socket.on("S2CWeatherReset", args -> {
            JSONObject payload = (JSONObject) args[0];
            MineboxAdditions.INSTANCE.state.getWeatherState().clear();
            applyTimestampArray(payload.optJSONArray("rain"), false);
            applyTimestampArray(payload.optJSONArray("storm"), true);
        });

        socket.on("S2CMermaidUpdate", args -> applyMermaidUpdate((JSONObject) args[0]));

        socket.on("S2CAck", args -> {
            JSONObject payload = (JSONObject) args[0];
            if (!"OK".equals(payload.optString("status"))) {
                System.out.println("[MineboxAdditions] S2CAck " + payload.optString("id") + " -> " + payload.optString("status") + " (" + payload.optString("error") + ")");
            }
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

        socket.on("S2CMineboxApiUnauthorized", args -> Utils.displayChatErrorMessage(Component
                .translatable("mineboxadditions.strings.errors.unauthorized-api").getString()));

        socket.on("S2CMissingMuseumItems", args -> {
            List<String> itemIds = new ArrayList<>();
            JSONArray arr = (JSONArray) args[0];
            for (int i = 0; i < arr.length(); i++) {
                String id = arr.optString(i, null);
                if (id != null && !id.isEmpty()) itemIds.add(id);
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

    private static void applyWeatherEntry(String type, long timestamp) {
        switch (type) {
            case "RAIN" -> MineboxAdditions.INSTANCE.state.getWeatherState().addRainTimestamp((int) timestamp);
            case "STORM" -> {
                MineboxAdditions.INSTANCE.state.getWeatherState().addRainTimestamp((int) timestamp); // Storms also count as rain :)
                MineboxAdditions.INSTANCE.state.getWeatherState().addStormTimestamp((int) timestamp);
            }
            default -> System.out.println("[MineboxAdditions] Received unknown weather entry type: " + type);
        }
    }

    private static void applyTimestampArray(JSONArray timestamps, boolean isStorm) {
        if (timestamps == null) return;
        for (int i = 0; i < timestamps.length(); i++) {
            applyWeatherEntry(isStorm ? "STORM" : "RAIN", timestamps.optLong(i));
        }
    }

    private static void applyMermaidUpdate(JSONObject mermaid) {
        int quantity = mermaid.optInt("quantity", 0);
        JSONObject translation = mermaid.optJSONObject("translation");
        String key = translation != null ? translation.optString("key", null) : null;
        JSONArray args = translation != null ? translation.optJSONArray("args") : null;
        String firstArg = args != null && args.length() > 0 ? args.optString(0, null) : null;
        ShopManager.getMermaid().set(quantity, key, firstArg);
    }
}
