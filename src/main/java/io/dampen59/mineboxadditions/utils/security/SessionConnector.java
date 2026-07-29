package io.dampen59.mineboxadditions.utils.security;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.EventLoopGroupHolder;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class SessionConnector {
    private static final String HOST = "session.mineboxadditions.bartier.me";
    private static final int PORT = 25565;

    public static void fetch(Consumer<String> callback) {
        Thread thread = new Thread(() -> {
            try {
                Minecraft client = Minecraft.getInstance();
                InetSocketAddress address = new InetSocketAddress(HOST, PORT);
                Connection connection = Connection.connectToServer(address, EventLoopGroupHolder.remote(false), null);
                SessionLoginPacketListener listener = new SessionLoginPacketListener(connection, client, callback);
                connection.initiateServerboundPlayConnection(HOST, PORT, listener);
                connection.send(new ServerboundHelloPacket(
                        client.getUser().getName(),
                        client.getUser().getProfileId()
                ));
            } catch (Throwable e) {
                callback.accept(null);
            }
        }, "mba-session-connector");
        thread.setDaemon(true);
        thread.start();
    }
}
