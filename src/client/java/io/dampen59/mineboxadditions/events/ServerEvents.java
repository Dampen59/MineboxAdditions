package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;

import java.util.Arrays;
import java.util.List;

public class ServerEvents {

    private List<String> mineboxHostnames = Arrays.asList("minebox.fr", "minebox.co");
    private State modState = null;

    public ServerEvents(State prmModState) {
        this.modState = prmModState;
        onServerJoinEvent();
        onServerLeaveEvent();
    }

    public void onServerJoinEvent() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
                String serverAddress = MinecraftClient.getInstance().getCurrentServerEntry().address;
                if (isMinebox(serverAddress)) {
                    ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

                    this.modState.setConnectedToMinebox(true);

                    if (config.networkFeatures.enableNetworkFeatures) {
                        this.modState.getSocket().connect();
                    }

                    if (config.autoIslandOnLogin == true) {
                        if (this.modState.getLoginCommandSent() == false) {
                            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("is"));
                            this.modState.setLoginCommandSent(true);
                        }
                    }
                } else {
                    this.modState.setConnectedToMinebox(false);
                }
            } else {
                // Single player
                this.modState.setConnectedToMinebox(false);
            }
        });

    }

    public void onServerLeaveEvent() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {

            if (this.modState.getSocket().connected()) {
                this.modState.getSocket().disconnect();
            }

            this.modState.reset();

        });
    }

    public boolean isMinebox(String hostname) {
        return mineboxHostnames.stream().anyMatch(hostname::contains);
    }

}
