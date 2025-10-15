package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.SocketManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;

import java.util.List;
import java.util.Objects;

public class ServerEvents {
    private static final List<String> MINEBOX_HOSTNAMES = List.of("minebox.fr", "minebox.co");
    private final State modState;

    public ServerEvents(State modState) {
        this.modState = modState;
        registerServerJoinEvent();
        registerServerLeaveEvent();
    }

    private void registerServerJoinEvent() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            var serverEntry = client.getCurrentServerEntry();
            if (serverEntry != null) {
                String serverAddress = serverEntry.address;
                if (isMineboxServer(serverAddress)) {
                    SocketManager.getSocket().connect();
                }
            }
        });
    }

    private void registerServerLeaveEvent() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (SocketManager.getSocket().connected()) {
                SocketManager.getSocket().disconnect();
                this.modState.getAudioManager().closeMicrophoneAndSpeaker();
            }
            modState.reset();
        });
    }

    private boolean isMineboxServer(String hostname) {
        String lowerHostname = hostname.toLowerCase();
        return MINEBOX_HOSTNAMES.stream().anyMatch(lowerHostname::contains);
    }
}
