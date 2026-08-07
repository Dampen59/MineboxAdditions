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
import java.util.regex.Pattern;

public class SessionConnector {
    private static final String HOST = "session.mineboxadditions.bartier.me";
    private static final int PORT = 25565;
    private static final long TIMEOUT_MS = 10_000;
    private static final Pattern CHALLENGE_PATTERN = Pattern.compile("^[0-9a-f]{32}$");

    public static void fetch(Consumer<String> callback) {
        fetch(null, callback);
    }

    public static void fetch(String challenge, Consumer<String> callback) {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<Thread> watchdogRef = new AtomicReference<>();
        Consumer<String> guarded = token -> {
            if (done.compareAndSet(false, true)) {
                Thread watchdogThread = watchdogRef.get();
                if (watchdogThread != null) watchdogThread.interrupt();
                callback.accept(token);
            }
        };
        AtomicReference<Connection> connRef = new AtomicReference<>();

        String declaredHost = (challenge != null && CHALLENGE_PATTERN.matcher(challenge).matches())
                ? challenge + "." + HOST
                : HOST;

        Thread thread = new Thread(() -> {
            try {
                Minecraft client = Minecraft.getInstance();
                InetSocketAddress address = new InetSocketAddress(HOST, PORT);
                Connection connection = Connection.connectToServer(address, EventLoopGroupHolder.remote(false), null);
                connRef.set(connection);
                SessionLoginPacketListener listener = new SessionLoginPacketListener(connection, client, guarded);
                connection.initiateServerboundPlayConnection(declaredHost, PORT, listener);
                connection.send(new ServerboundHelloPacket(
                        client.getUser().getName(),
                        client.getUser().getProfileId()
                ));
            } catch (Throwable e) {
                guarded.accept(null);
            }
        }, "mba-session-connector");
        thread.setDaemon(true);

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
        watchdogRef.set(watchdog);
        thread.start();
        watchdog.start();
    }
}
