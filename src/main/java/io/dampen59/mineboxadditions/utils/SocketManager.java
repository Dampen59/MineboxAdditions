package io.dampen59.mineboxadditions.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.maxhenkel.opus4j.OpusDecoder;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.voicechat.AudioManager;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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

        socket.on("S2CAudioData", args -> {
            String playerName = (String) args[0];
            byte[] audioData = (byte[]) args[1];
            try {

                OpusDecoder decoder = MineboxAdditions.INSTANCE.state.getAudioManager().getDecoders().computeIfAbsent(playerName,
                        name -> {
                            try {
                                return new OpusDecoder(48000, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        });

                if (decoder == null)
                    return;

                AudioUtils.playAudio(decoder, audioData, playerName);

            } catch (Exception e) {
                e.printStackTrace();
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

        socket.on("S2CProximityAudioData", args -> {
            String playerName = (String) args[0];
            byte[] audioData = (byte[]) args[1];

            try {
                PlayerEntity sourcePlayer = AudioUtils.getNearbyPlayer(playerName);
                if (sourcePlayer == null)
                    return;

                float volumeMultiplier = AudioUtils.computeVolumeMultiplier(sourcePlayer);

                OpusDecoder decoder = MineboxAdditions.INSTANCE.state.getAudioManager().getDecoders().computeIfAbsent(playerName,
                        name -> {
                            try {
                                return new OpusDecoder(48000, 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        });

                if (decoder == null)
                    return;

                AudioUtils.playProximityAudio(decoder, audioData, volumeMultiplier, sourcePlayer, playerName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        socket.on("S2CAudioRoomCreated", args -> {
            String roomCode = (String) args[0];
            Utils.displayChatSuccessMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.create.success", roomCode).getString());

            try {
                AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();

                Mixer speakerMixer = AudioUtils
                        .getMixerByName(Config.selectedSpeakerName);
                if (speakerMixer != null) {
                    audioManager.openSpeaker(speakerMixer);
                } else {
                    audioManager.openSpeaker();
                }

                Mixer micMixer = AudioUtils.getMixerByName(Config.selectedMicName);
                if (micMixer != null) {
                    audioManager.openMicrophone(micMixer);
                } else {
                    audioManager.openMicrophone();
                }

            } catch (LineUnavailableException e) {
                SocketManager.getSocket().emit("C2SLeaveAudioRoom");
                Utils.displayChatErrorMessage(
                        "You have left the voice channel because MineboxAdditions was not able to setup your Speakers and/or Microphone. Please check your game logs.");
                MineboxAdditions.LOGGER.error("[SocketManager] Failed to open Speaker or Microphone : {}",
                        e.getMessage());
            }
        });

        socket.on("S2CAudioRoomJoined", args -> {
            String roomCode = (String) args[0];
            Utils.displayChatSuccessMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.join.success", roomCode).getString());

            try {
                AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();

                Mixer speakerMixer = AudioUtils
                        .getMixerByName(Config.selectedSpeakerName);
                if (speakerMixer != null) {
                    audioManager.openSpeaker(speakerMixer);
                } else {
                    audioManager.openSpeaker();
                }

                Mixer micMixer = AudioUtils.getMixerByName(Config.selectedMicName);
                if (micMixer != null) {
                    audioManager.openMicrophone(micMixer);
                } else {
                    audioManager.openMicrophone();
                }

            } catch (LineUnavailableException e) {
                SocketManager.getSocket().emit("C2SLeaveAudioRoom");
                Utils.displayChatErrorMessage(
                        "You have left the voice channel because MineboxAdditions was not able to setup your Speakers and/or Microphone. Please check your game logs.");
                MineboxAdditions.LOGGER.error("[SocketManager] Failed to open Speaker or Microphone : {}",
                        e.getMessage());
            }

        });

        socket.on("S2CProximityAudioToggled", args -> {
            boolean isEnabled = (boolean) args[0];

            if (isEnabled) {
                Utils.displayChatSuccessMessage(
                        Text.translatable("mineboxadditions.strings.audiochannel.proximity.enabled").getString());

                AudioManager audioManager = MineboxAdditions.INSTANCE.state.getAudioManager();

                try {
                    if (audioManager.getSpeaker() == null || !audioManager.getSpeaker().isOpen()) {
                        Mixer speakerMixer = AudioUtils
                                .getMixerByName(Config.selectedSpeakerName);
                        if (speakerMixer != null) {
                            audioManager.openSpeaker(speakerMixer);
                        } else {
                            audioManager.openSpeaker();
                        }
                    }

                    if (audioManager.getMicrophone() == null || !audioManager.getMicrophone().isOpen()) {
                        Mixer micMixer = AudioUtils
                                .getMixerByName(Config.selectedMicName);
                        if (micMixer != null) {
                            audioManager.openMicrophone(micMixer);
                        } else {
                            audioManager.openMicrophone();
                        }
                    }

                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }

            } else {
                MineboxAdditions.INSTANCE.state.getAudioManager().closeMicrophoneAndSpeaker();
                Utils.displayChatErrorMessage(
                        Text.translatable("mineboxadditions.strings.audiochannel.proximity.disabled").getString());
            }
        });

        socket.on("S2CJoinAudioRoomFailed", args -> {
            String roomCode = (String) args[0];
            Utils.displayChatErrorMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.join.failed", roomCode).getString());
        });

        socket.on("S2CAudioRoomCreationFailed", args -> {
            Utils.displayChatErrorMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.create.failed").getString());
        });

        socket.on("S2CAudioClientConnected", args -> {
            String playerName = (String) args[0];
            Utils.displayChatInfoMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.user.connected", playerName).getString());
        });

        socket.on("S2CLeaveAudioRoomFailed", args -> {
            Utils.displayChatErrorMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.leave.failed").getString());
        });

        socket.on("S2CEnableProximityAudioFailedLeaveFirst", args -> {
            Utils.displayChatErrorMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.proximity.enable.fail.leavefirst")
                            .getString());
        });

        socket.on("S2CCreateJoinAudioRoomFailedDisableProximityFirst", args -> {
            Utils.displayChatErrorMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.join.fail.leavefirst").getString());
        });

        socket.on("S2CAudioRoomLeft", args -> {
            String roomCode = (String) args[0];
            Utils.displayChatSuccessMessage(
                    Text.translatable("mineboxadditions.strings.audiochannel.leave.success", roomCode).getString());
            MineboxAdditions.INSTANCE.state.getAudioManager().closeMicrophoneAndSpeaker();
        });

        socket.on("S2CAudioClientDisconnected", args -> {
            String playerName = (String) args[0];
            Utils.displayChatInfoMessage(Text
                    .translatable("mineboxadditions.strings.audiochannel.user.disconnected", playerName).getString());
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
