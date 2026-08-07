package io.dampen59.mineboxadditions.utils.security;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.login.*;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

import io.netty.channel.ChannelFutureListener;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SessionLoginPacketListener implements ClientLoginPacketListener {
    private final Connection connection;
    private final Minecraft client;
    private final Consumer<String> callback;
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicBoolean kickReceived = new AtomicBoolean(false);

    public SessionLoginPacketListener(Connection connection, Minecraft client, Consumer<String> callback) {
        this.connection = connection;
        this.client = client;
        this.callback = callback;
    }

    @Override
    public void handleHello(ClientboundHelloPacket packet) {
        SecretKey secretKey;
        try {
            secretKey = Crypt.generateSecretKey();
        } catch (Exception e) {
            complete(null);
            connection.disconnect(Component.literal("Auth error"));
            return;
        }
        PublicKey publicKey = null;
        try {
            publicKey = packet.getPublicKey();
        } catch (CryptException e) {
            throw new RuntimeException(e);
        }

        PublicKey finalPublicKey = publicKey;
        Thread authThread = new Thread(() -> {
            try {
                String serverId = new BigInteger(Crypt.digestData(packet.getServerId(), finalPublicKey, secretKey)).toString(16);
                GameProfile profile = new GameProfile(client.getUser().getProfileId(), client.getUser().getName());
                client.services().sessionService().joinServer(profile.id(), client.getUser().getAccessToken(), serverId);

                connection.send(new ServerboundKeyPacket(secretKey, finalPublicKey, packet.getChallenge()), (ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        complete(null);
                        connection.disconnect(Component.literal("Key packet write failed"));
                        return;
                    }
                    try {
                        connection.setEncryptionKey(Crypt.getCipher(2, secretKey), Crypt.getCipher(1, secretKey));
                    } catch (Exception e) {
                        complete(null);
                        connection.disconnect(Component.literal("Encryption setup error"));
                    }
                });

            } catch (Exception e) {
                complete(null);
                connection.disconnect(Component.literal("Auth error"));
            }
        }, "mba-session-auth");
        authThread.setDaemon(true);
        authThread.start();
    }

    @Override
    public void handleDisconnect(ClientboundLoginDisconnectPacket packet) {
        kickReceived.set(true);
        complete(packet.reason().getString());
    }

    @Override
    public void handleCompression(ClientboundLoginCompressionPacket packet) {
        connection.setupCompression(packet.getCompressionThreshold(), false);
    }

    @Override
    public void handleLoginFinished(ClientboundLoginFinishedPacket packet) {
        connection.send(ServerboundLoginAcknowledgedPacket.INSTANCE);
        connection.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND,
                new SessionConfigPacketListener(connection, kickReceived, this::complete));
        connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
    }

    @Override
    public void handleCustomQuery(ClientboundCustomQueryPacket packet) {
        connection.send(new ServerboundCustomQueryAnswerPacket(packet.transactionId(), null));
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        if (!kickReceived.get()) {
            complete(null);
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return connection.isConnected();
    }

    private void complete(String sessionToken) {
        if (completed.compareAndSet(false, true)) {
            callback.accept(sessionToken);
        }
    }

    @Override
    public void handleRequestCookie(ClientboundCookieRequestPacket packet) {
        connection.send(new ServerboundCookieResponsePacket(packet.key(), null));
    }
}
