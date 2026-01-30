package io.dampen59.mineboxadditions.features;

import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.utils.models.Location;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;

import java.util.Objects;

public class AutoIsland {
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(AutoIsland::onJoin);
    }

    private static void onJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (!Utils.isOnMinebox()) return;
        if (Config.autoIsland && Utils.getPreviousLocation() == Location.UNKNOWN) {
            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(new CommandExecutionC2SPacket("is"));
        }
    }
}
