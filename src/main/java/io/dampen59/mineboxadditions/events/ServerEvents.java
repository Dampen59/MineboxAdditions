package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ApiUtils;
import io.dampen59.mineboxadditions.utils.security.SessionConnector;
import io.dampen59.mineboxadditions.utils.SocketManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.List;

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
            var serverEntry = client.getCurrentServer();
            if (serverEntry != null) {
                String serverAddress = serverEntry.ip;
                if (isMineboxServer(serverAddress)) {
                    SessionConnector.fetch(SocketManager::connectWithSessionToken);
                }
            }
        });
    }

    private void registerServerLeaveEvent() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SocketManager.getSocket().disconnect();
            modState.reset();
        });
    }

    private boolean isMineboxServer(String hostname) {
        String lowerHostname = hostname.toLowerCase();
        return MINEBOX_HOSTNAMES.stream().anyMatch(lowerHostname::contains);
    }
}
