package io.dampen59.mineboxadditions.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SocketManager {
    private static Socket socket;
    private static final int protocol = 10;
    private static final ObjectMapper mapper = new ObjectMapper();

    @NotNull
    public static Socket getSocket() {
        if (socket == null) init();
        return socket;
    }

    public static void init() {
        socket = IO.socket(URI.create("https://mineboxadditions.bartier.me"), IO.Options.builder().build());

        socket.on(Socket.EVENT_CONNECT, args -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                String playerName = client.player.getName().getString();
                String playerUuid = client.player.getUuid().toString();
                String playerLang = client.getLanguageManager().getLanguage();
                socket.emit("C2SHelloConnectMessage", playerUuid, playerName, playerLang, protocol);
            }
        });

        socket.on("S2CProtocolMismatch", args -> Utils.showToastNotification(
                Text.translatable("mineboxadditions.strings.update.title").getString(),
                Text.translatable("mineboxadditions.strings.update.content").getString()));

        socket.on("S2CMineboxItemsStats", args -> {
            String jsonData = (String) args[0];
            try {
                List<MineboxItem> itemsList = mapper.readValue(jsonData, mapper.getTypeFactory().constructCollectionType(List.class, MineboxItem.class));
                MineboxAdditions.INSTANCE.state.setMbxItems(itemsList);
            } catch (JsonProcessingException e) {
                System.out.println("[SocketManager] Failed to load Minebox Items Stats JSON: " + e.getMessage());
            }
        });

        socket.on("S2CHarvestableData", args -> {
            String islandName = (String) args[0];
            String jsonData = (String) args[1];
            try {
                List<Harvestable> items = mapper.readValue(jsonData,
                        mapper.getTypeFactory().constructCollectionType(List.class, Harvestable.class));
                MineboxAdditions.INSTANCE.state.addMineboxHarvestables(islandName, items);
            } catch (Exception e) {
                System.out.println("[SocketManager] Failed to load Harvestables JSON: " + e.getMessage());
            }
        });

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

        socket.on("S2CMermaidRequest", args -> {
            int itemQuantity = (int) args[0];
            String itemTranslationKey = (String) args[1];
            String itemTranslationKeyArgs = (args[2] instanceof String) ? (String) args[2] : null;
            ShopManager.getMermaid().set(itemQuantity, itemTranslationKey, itemTranslationKeyArgs);
        });

        socket.on("S2CMineboxApiUnauthorized", args -> {
            Utils.displayChatErrorMessage(Text
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

    }
}
