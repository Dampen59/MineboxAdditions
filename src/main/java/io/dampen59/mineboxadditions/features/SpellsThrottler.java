package io.dampen59.mineboxadditions.features;

import io.dampen59.mineboxadditions.config.spells.SpellsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellsThrottler {
    private static final long[] cooldownEndTime = new long[4];
    private static int lastAttemptedSpellId = -1;

    // EN: "The spell is on cooldown! 89.1s remaining."
    // FR: "Le sort est en cours de rechargement ! 88.7 restantes."
    private static final Pattern COOLDOWN_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)s?\\s*(?:remaining|restantes)",
            Pattern.CASE_INSENSITIVE
    );

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            String text = message.getString();
            if (!text.contains("cooldown") && !text.contains("rechargement")) return;

            Matcher m = COOLDOWN_PATTERN.matcher(text);
            if (m.find() && lastAttemptedSpellId >= 1) {
                double remaining = Double.parseDouble(m.group(1));
                cooldownEndTime[lastAttemptedSpellId - 1] = System.currentTimeMillis() + (long) (remaining * 1000);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    public static void tryCast(int spellId, Minecraft client) {
        lastAttemptedSpellId = spellId;

        if (isOnCooldown(spellId)) {
            if (SpellsConfig.cooldownMessages) {
                long remainingMs = cooldownEndTime[spellId - 1] - System.currentTimeMillis();
                Utils.displayChatInfoMessage(Component.translatable(
                        "mineboxadditions.strings.spells.cooldown",
                        String.format("%.1f", remainingMs / 1000.0)
                ).getString());
            }
            return;
        }

        if (client.player == null || client.getConnection() == null) return;
        Objects.requireNonNull(client.getConnection()).send(new ServerboundChatCommandPacket("cast " + spellId));
    }

    public static boolean isOnCooldown(int spellId) {
        return System.currentTimeMillis() < cooldownEndTime[spellId - 1];
    }

    private static void reset() {
        for (int i = 0; i < 4; i++) {
            cooldownEndTime[i] = 0;
        }
        lastAttemptedSpellId = -1;
    }
}
