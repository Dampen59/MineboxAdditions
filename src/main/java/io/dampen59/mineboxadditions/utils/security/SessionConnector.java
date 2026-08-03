package io.dampen59.mineboxadditions.utils.security;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.EventLoopGroupHolder;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SessionConnector {
    private static final String HOST = "session.mineboxadditions.bartier.me";
    private static final int PORT = 25565;
    private static final long TIMEOUT_MS = 10_000;

    public static void fetch(Consumer<String> callback) {
        AtomicBoolean done = new AtomicBoolean(false);
        Consumer<String> guarded = token -> {
            if (done.compareAndSet(false, true)) {
                callback.accept(token);
            }
        };
        AtomicReference<Connection> connRef = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                Minecraft client = Minecraft.getInstance();
                InetSocketAddress address = new InetSocketAddress(HOST, PORT);
                Connection connection = Connection.connectToServer(address, EventLoopGroupHolder.remote(false), null);
                connRef.set(connection);
                SessionLoginPacketListener listener = new SessionLoginPacketListener(connection, client, guarded);
                connection.initiateServerboundPlayConnection(HOST, PORT, listener);
                connection.send(new ServerboundHelloPacket(
                        client.getUser().getName(),
                        client.getUser().getProfileId()
                ));
            } catch (Throwable e) {
                guarded.accept(null);
            }
        }, "mba-session-connector");
        thread.setDaemon(true);
        thread.start();

        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
            } catch (InterruptedException ignored) {
                return;
            }
            if (!done.get()) {
                Connection connection = connRef.get();
                if (connection != null) connection.disconnect(Component.literal("Session fetch timed out"));
                guarded.accept(null);
            }
        }, "mba-session-timeout");
        watchdog.setDaemon(true);
        watchdog.start();
    }
}
