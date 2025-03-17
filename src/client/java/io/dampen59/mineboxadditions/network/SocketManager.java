package io.dampen59.mineboxadditions.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.minebox.MineboxChatFlag;
import io.dampen59.mineboxadditions.minebox.MineboxFishingShoal;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import io.socket.client.IO;
import io.socket.client.Socket;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class SocketManager {
    private final State modState;
    private final int protocolVersion = 5;
    private final ObjectMapper mapper = new ObjectMapper();

    public SocketManager(State modState) {
        this.modState = modState;
        initializeSocket();
    }

    private void initializeSocket() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        URI uri = URI.create(config.socketServerAddress);
        IO.Options options = IO.Options.builder().build();
        Socket socket = IO.socket(uri, options);
        modState.setSocket(socket);

        socket.on("S2CShopOfferEvent", args -> {
            String shopName = (String) args[0];
            String itemName = (String) args[1];
            switch (shopName) {
                case "Mouse":
                    if (modState.getMouseCurrentItemOffer() == null) {
                        String title = Text.translatable("mineboxadditions.strings.toasts.shop.mouse.iteminfo.title").getString();
                        modState.setMouseCurrentItemOffer(title + ": " + itemName);
                        Utils.showShopToastNotification("MOUSE", title, Text.translatable("mineboxadditions.strings.toasts.shop.mouse.iteminfo.content", itemName).getString());
                        Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                    }
                    break;
                case "Bakery":
                    if (modState.getBakeryCurrentItemOffer() == null) {
                        String title = Text.translatable("mineboxadditions.strings.toasts.shop.bakery.iteminfo.title").getString();
                        modState.setBakeryCurrentItemOffer(title + ": " + itemName);
                        Utils.showShopToastNotification("BAKERY", title, Text.translatable("mineboxadditions.strings.toasts.shop.bakery.iteminfo.content", itemName).getString());
                        Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                    }
                    break;
                case "Buckstar":
                    if (modState.getBuckstarCurrentItemOffer() == null) {
                        String title = Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.iteminfo.title").getString();
                        modState.setBuckstarCurrentItemOffer(title + ": " + itemName);
                        Utils.showShopToastNotification("BUCKSTAR", title, Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.iteminfo.content", itemName).getString());
                        Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                    }
                    break;
                case "Cocktail":
                    if (modState.getCocktailCurrentItemOffer() == null) {
                        String title = Text.translatable("mineboxadditions.strings.toasts.shop.cocktail.iteminfo.title").getString();
                        modState.setCocktailCurrentItemOffer(title + ": " + itemName);
                        Utils.showShopToastNotification("COCKTAIL", title, Text.translatable("mineboxadditions.strings.toasts.shop.cocktail.iteminfo.content", itemName).getString());
                        Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                    }
                    break;
                default:
                    System.out.println("[SocketManager] Unknown shop event: " + shopName + " Data: " + Arrays.toString(args));
                    break;
            }
        });

        socket.on("S2CProtocolMismatch", args ->
                Utils.showToastNotification(
                        Text.translatable("mineboxadditions.strings.update.title").getString(),
                        Text.translatable("mineboxadditions.strings.update.content").getString()
                )
        );

        socket.on("S2CMineboxItemsStats", args -> {
            String jsonData = (String) args[0];
            try {
                List<MineboxItem> items = mapper.readValue(jsonData,
                        mapper.getTypeFactory().constructCollectionType(List.class, MineboxItem.class));
                modState.setMbxItems(items);
            } catch (JsonProcessingException e) {
                System.out.println("[SocketManager] Failed to load Minebox Items Stats JSON: " + e.getMessage());
            }
        });

        socket.on("S2CMineboxChatFlags", args -> {
            String jsonData = (String) args[0];
            try {
                List<MineboxChatFlag> items = mapper.readValue(jsonData,
                        mapper.getTypeFactory().constructCollectionType(List.class, MineboxChatFlag.class));
                modState.setMbxChatFlags(items);
            } catch (JsonProcessingException e) {
                System.out.println("[SocketManager] Failed to load Minebox Chat Flags JSON: " + e.getMessage());
            }
        });

        socket.on("S2CMineboxFishables", args -> {
            String jsonData = (String) args[0];
            try {
                List<MineboxFishingShoal.FishingShoalFish> items = mapper.readValue(jsonData,
                        mapper.getTypeFactory().constructCollectionType(List.class, MineboxFishingShoal.FishingShoalFish.class));
                modState.setMbxFishables(items);
            } catch (JsonProcessingException e) {
                System.out.println("[SocketManager] Failed to load Minebox Fishables JSON: " + e.getMessage());
            }
        });

        socket.on("S2CShinyEvent", args -> {
            String playerName = (String) args[0];
            String mobKey = (String) args[1];
            String mobUuid = (String) args[2];

            modState.getMbxShiniesUuids().put(mobUuid, true);
            String mobName = Text.translatable(mobKey).getString();
            Utils.shinyFoundAlert(playerName, mobName);
        });

        socket.on("S2CChatMessage", args -> {

            if (!config.chatSettings.multiChannelSettings.enableMultiChannel) return;

            String sourceChatLang = (String) args[0];
            String playerName = (String) args[1];
            String msgContent = (String) args[2];

            if (Objects.equals(modState.getChatLang(), sourceChatLang)) return;
            if (!config.chatSettings.multiChannelSettings.enableFrench && Objects.equals(sourceChatLang, "fr")) return;
            if (!config.chatSettings.multiChannelSettings.enableEnglish && Objects.equals(sourceChatLang, "en")) return;
            if (!config.chatSettings.multiChannelSettings.enableSpanish && Objects.equals(sourceChatLang, "es")) return;
            if (!config.chatSettings.multiChannelSettings.enableRussian && Objects.equals(sourceChatLang, "ru")) return;
            if (!config.chatSettings.multiChannelSettings.enablePortuguese && Objects.equals(sourceChatLang, "pt")) return;
            if (!config.chatSettings.multiChannelSettings.enableDeutsch && Objects.equals(sourceChatLang, "de")) return;
            if (!config.chatSettings.multiChannelSettings.enableChinese && Objects.equals(sourceChatLang, "cn")) return;
            if (!config.chatSettings.multiChannelSettings.enablePolish && Objects.equals(sourceChatLang, "pl")) return;
            if (!config.chatSettings.multiChannelSettings.enableItalian && Objects.equals(sourceChatLang, "it")) return;
            if (!config.chatSettings.multiChannelSettings.enableJapanese && Objects.equals(sourceChatLang, "jp")) return;
            if (!config.chatSettings.multiChannelSettings.enableDutch && Objects.equals(sourceChatLang, "nl")) return;
            if (!config.chatSettings.multiChannelSettings.enableTurkish && Objects.equals(sourceChatLang, "tr")) return;

            String sourceLangFlag = Utils.getChatFlagByLang(modState.getMbxChatFlags(), sourceChatLang);
            Utils.displayChatMessage(sourceLangFlag, playerName, msgContent);

        });

        socket.on(Socket.EVENT_CONNECT, args -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                String playerName = client.player.getName().getString();
                String playerUuid = client.player.getUuid().toString();
                String playerLang = client.getLanguageManager().getLanguage();
                socket.emit("C2SHelloConnectMessage", playerUuid, playerName, playerLang, protocolVersion);
            }
        });
    }


    // manual control over when to open or close the socket connection, not used rn
    public void connect() {
        Socket socket = modState.getSocket();
        if (socket != null && !socket.connected()) {
            socket.connect();
        }
    }

    public void disconnect() {
        Socket socket = modState.getSocket();
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }
}
