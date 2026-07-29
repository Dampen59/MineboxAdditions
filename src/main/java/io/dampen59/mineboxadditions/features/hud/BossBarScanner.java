package io.dampen59.mineboxadditions.features.hud;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.MineboxDefaultHuds;
import io.dampen59.mineboxadditions.mixins.BossHealthOverlayAccessor;
import io.dampen59.mineboxadditions.state.State;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class BossBarScanner {
    private static final Pattern PURE_TIME = Pattern.compile("^\\d{1,2}:\\d{2}$");
    private static final Pattern EMBEDDED_TIME = Pattern.compile("\\d{1,2}:\\d{2}");

    public BossBarScanner() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        Component island = null, time = null, keyFragment = null;
        Component stats = null, freeItem = null, votes = null;

        if (client.gui != null && client.level != null) {
            for (LerpingBossEvent event : ((BossHealthOverlayAccessor) client.gui.hud.getBossOverlay()).mbx$getEvents().values()) {
                List<MutableComponent> segments = splitSegments(event.getName());
                List<String> plains = new ArrayList<>(segments.size());
                for (MutableComponent seg : segments) plains.add(cleanPlain(seg));

                boolean mainBar = plains.stream().anyMatch(p -> PURE_TIME.matcher(p).matches());
                boolean infoBar = plains.stream().anyMatch(p ->
                        p.contains("/stats") || p.contains("/store") || p.contains("/vote"));

                for (int i = 0; i < segments.size(); i++) {
                    String plain = plains.get(i);
                    if (plain.isEmpty()) continue;
                    MutableComponent seg = segments.get(i);

                    if (mainBar) {
                        if (PURE_TIME.matcher(plain).matches()) time = seg;
                        else if (EMBEDDED_TIME.matcher(plain).find()) keyFragment = seg;
                        else if (plain.chars().anyMatch(Character::isLetter)) island = seg;
                    } else if (infoBar) {
                        if (plain.contains("/stats")) stats = seg;
                        else if (plain.contains("/store")) freeItem = seg;
                        else if (plain.contains("/vote")) votes = seg;
                    }
                }
            }
        }

        State state = MineboxAdditions.INSTANCE.state;
        state.setBossbarIsland(island);
        state.setBossbarTime(time);
        state.setBossbarKeyFragment(keyFragment);
        state.setBossbarStatsPoints(stats);
        state.setBossbarFreeItem(freeItem);
        state.setBossbarVotes(votes);
    }

    private static List<MutableComponent> splitSegments(Component name) {
        List<MutableComponent> out = new ArrayList<>();
        MutableComponent[] current = { null };

        FormattedText.StyledContentConsumer<Object> consumer = (style, str) -> {
            StringBuilder buf = new StringBuilder();
            for (char c : str.toCharArray()) {
                if (c >= 0xE000 && c <= 0xF8FF && c != 0xF802) {
                    appendRun(current, buf, style);
                    if (current[0] != null) {
                        out.add(current[0]);
                        current[0] = null;
                    }
                } else {
                    buf.append(c);
                }
            }
            appendRun(current, buf, style);
            return Optional.empty();
        };
        name.visit(consumer, Style.EMPTY);

        if (current[0] != null) out.add(current[0]);
        return out;
    }

    public static boolean isHandledBar(Component name) {
        boolean main = false, info = false;
        for (MutableComponent seg : splitSegments(name)) {
            String plain = cleanPlain(seg);
            if (PURE_TIME.matcher(plain).matches()) main = true;
            if (plain.contains("/stats") || plain.contains("/store") || plain.contains("/vote")) info = true;
        }
        if (main) return MineboxDefaultHuds.island || MineboxDefaultHuds.time || MineboxDefaultHuds.keyfragment;
        if (info) return MineboxDefaultHuds.statspoints || MineboxDefaultHuds.freeitem || MineboxDefaultHuds.vote;
        return false;
    }

    private static String cleanPlain(Component seg) {
        StringBuilder sb = new StringBuilder();
        for (char c : seg.getString().toCharArray()) {
            if (Character.isSurrogate(c) || (c >= 0xE000 && c <= 0xF8FF)) continue;
            sb.append(c);
        }
        return sb.toString().trim();
    }

    private static void appendRun(MutableComponent[] current, StringBuilder buf, Style style) {
        if (buf.length() == 0) return;
        MutableComponent part = Component.literal(buf.toString()).setStyle(style);
        if (current[0] == null) current[0] = Component.empty();
        current[0].append(part);
        buf.setLength(0);
    }
}
