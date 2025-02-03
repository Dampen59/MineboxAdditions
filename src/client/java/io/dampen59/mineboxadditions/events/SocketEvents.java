package io.dampen59.mineboxadditions.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.utils.Utils;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import io.dampen59.mineboxadditions.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.text.Text;

import java.net.URI;
import java.util.List;

public class SocketEvents {
    private State modState = null;
    public static final int protocolVersion = 2;

    public SocketEvents(State prmModState) {
        this.modState = prmModState;
        initializeSockets();
    }

    public void initializeSockets() {

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        URI uri = URI.create(config.socketServerAddress);
        IO.Options options = IO.Options.builder().build();
        this.modState.setSocket(IO.socket(uri, options));

        this.modState.getSocket().on("S2CShopOfferEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                if (!config.networkFeatures.receiveShopsAlerts) return;

                String shopName = (String) args[0];
                String itemName = (String) args[1];

                switch (shopName) {
                    case "Mouse":
                        if (modState.getMouseCurrentItemOffer() == null) {
                            modState.setMouseCurrentItemOffer(Text.translatable("mineboxadditions.strings.toasts.shop.mouse.iteminfo.title").getString() + ": " + itemName);
                            Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.mouse.iteminfo.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.mouse.iteminfo.content", itemName).getString());
                            Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                        }
                        break;
                    case "Bakery":
                        if (modState.getBakeryCurrentItemOffer() == null) {
                            modState.setBakeryCurrentItemOffer(Text.translatable("mineboxadditions.strings.toasts.shop.bakery.iteminfo.title").getString() + ": " + itemName);
                            Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.bakery.iteminfo.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.bakery.iteminfo.content", itemName).getString());
                            Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                        }
                        break;
                    case "Buckstar":
                        if (modState.getBuckstarCurrentItemOffer() == null) {
                            modState.setBuckstarCurrentItemOffer(Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.iteminfo.title").getString() + ": " + itemName);
                            Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.iteminfo.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.buckstar.iteminfo.content", itemName).getString());
                            Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                        }
                        break;
                    case "Cocktail":
                        if (modState.getCocktailCurrentItemOffer() == null) {
                            modState.setCocktailCurrentItemOffer(Text.translatable("mineboxadditions.strings.toasts.shop.cocktail.iteminfo.title").getString() + ": " + itemName);
                            Utils.showToastNotification(Text.translatable("mineboxadditions.strings.toasts.shop.cocktail.iteminfo.title").getString(), Text.translatable("mineboxadditions.strings.toasts.shop.cocktail.iteminfo.content", itemName).getString());
                            Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                        }
                        break;
                    default:
                        System.out.println("[S2CShopOfferEvent] Received unknown S2CShopOfferEvent payload data. Data : " + args);

                }

            }
        });

        this.modState.getSocket().on("S2CProtocolMismatch", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Utils.showToastNotification(Text.translatable("mineboxadditions.strings.update.title").getString(), Text.translatable("mineboxadditions.strings.update.content").getString());
            }
        });

        this.modState.getSocket().on("S2CMineboxItemsStats", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String jsonData = (String) args[0];

                ObjectMapper mapper = new ObjectMapper();
                try {
                    modState.setMbxItems(mapper.readValue(jsonData, mapper.getTypeFactory().constructCollectionType(List.class, MineboxItem.class)));
                } catch (JsonProcessingException e) {
                    System.out.println("[MineboxAdditions] Could not load Minebox Items Stats JSON data :( " + e.getMessage());
                }

            }
        });

        this.modState.getSocket().on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // Data used server-side to keep track of players throughout their session
                // It also allows to pass some metadata such as client lang to handle some server-side translations
                // and protocol version to know server-side how to communicate with our client
                // throughout the different versions.
                String playerName = MinecraftClient.getInstance().player.getName().getString();
                String playerUuid = MinecraftClient.getInstance().player.getUuid().toString();
                String playerLang = MinecraftClient.getInstance().getLanguageManager().getLanguage();
                modState.getSocket().emit("C2SHelloConnectMessage", playerUuid , playerName, playerLang, protocolVersion);
            }
        });
    }
}
