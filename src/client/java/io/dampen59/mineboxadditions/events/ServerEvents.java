package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
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
            MineboxAdditionConfig config = AutoConfig.getConfigHolder(MineboxAdditionConfig.class).getConfig();
            if (serverEntry != null) {
                String serverAddress = serverEntry.address;
                if (isMineboxServer(serverAddress)) {
                    modState.setConnectedToMinebox(true);
                    modState.getSocket().connect();
                    if (config.autoIslandOnLogin && !modState.isLoginCommandSent()) {
                        Objects.requireNonNull(client.getNetworkHandler()).sendPacket(new CommandExecutionC2SPacket("is"));
                        modState.setLoginCommandSent(true);
                    }
                } else {
                    modState.setConnectedToMinebox(false);
                }
            } else {
                modState.setConnectedToMinebox(false);
            }
        });
    }

    private void registerServerLeaveEvent() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (modState.getSocket().connected()) {
                modState.getSocket().disconnect();
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
