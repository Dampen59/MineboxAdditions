package io.dampen59.mineboxadditions.utils.security;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.configuration.*;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SessionConfigPacketListener implements ClientConfigurationPacketListener {
    private final Connection connection;
    private final AtomicBoolean kickReceived;
    private final Consumer<String> completeCallback;

    public SessionConfigPacketListener(Connection connection, AtomicBoolean kickReceived, Consumer<String> completeCallback) {
        this.connection = connection;
        this.kickReceived = kickReceived;
        this.completeCallback = completeCallback;
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        kickReceived.set(true);
        completeCallback.accept(packet.reason().getString());
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket packet) {
        connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
        completeCallback.accept(null);
    }

    @Override
    public void handleSelectKnownPacks(ClientboundSelectKnownPacks packet) {
        connection.send(new ServerboundSelectKnownPacks(List.of()));
    }

    @Override
    public void handleKeepAlive(ClientboundKeepAlivePacket packet) {
        connection.send(new ServerboundKeepAlivePacket(packet.getId()));
    }

    @Override
    public void handlePing(ClientboundPingPacket packet) {
        connection.send(new ServerboundPongPacket(packet.getId()));
    }

    @Override
    public void handleCodeOfConduct(ClientboundCodeOfConductPacket packet) {}

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket packet) {}

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket packet) {}

    @Override
    public void handleResetChat(ClientboundResetChatPacket packet) {}

    @Override
    public void handleCustomPayload(ClientboundCustomPayloadPacket packet) {}

    @Override
    public void handleResourcePackPush(ClientboundResourcePackPushPacket packet) {}

    @Override
    public void handleResourcePackPop(ClientboundResourcePackPopPacket packet) {}

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket packet) {}

    @Override
    public void handleStoreCookie(ClientboundStoreCookiePacket packet) {}

    @Override
    public void handleTransfer(ClientboundTransferPacket packet) {}

    @Override
    public void handleCustomReportDetails(ClientboundCustomReportDetailsPacket packet) {}

    @Override
    public void handleServerLinks(ClientboundServerLinksPacket packet) {}

    @Override
    public void handleClearDialog(ClientboundClearDialogPacket packet) {}

    @Override
    public void handleShowDialog(ClientboundShowDialogPacket packet) {}

    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket packet) {
        connection.send(new ServerboundCookieResponsePacket(packet.key(), null));
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        if (!kickReceived.get()) {
            completeCallback.accept(null);
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return connection.isConnected();
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.CLIENTBOUND;
    }
}
