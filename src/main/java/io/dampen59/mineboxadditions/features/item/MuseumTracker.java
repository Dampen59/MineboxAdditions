package io.dampen59.mineboxadditions.features.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Environment(EnvType.CLIENT)
public final class MuseumTracker {

    private static final String MUSEUM_CATALOG_URL = "https://api.minebox.co/museum";
    private static final String USER_DATA_URL = "https://api.minebox.co/data/%s";
    private static final long POLL_INTERVAL_SECONDS = 10L;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final Set<String> CROP_NAMES = Set.of(
            "bamboo", "beetroot", "cactus", "carrot", "cocoa_beans", "kelp",
            "melon_slice", "nether_wart", "potato", "pumpkin", "sugar_cane",
            "sweet_berries", "wheat");
    private static final Set<String> MATERIAL_PREFIXES = Set.of(
            "bag", "barrel", "crate", "enchanted", "transformed");

    private static final Set<String> IGNORED_IDS = Set.of(
            "staff_valentine_heart", "mount_default", "ship_default", "staff_lny_snake",
            "angel_helmet", "angel_backpack", "pet_lny_snake", "mount_lny_snake",
            "pet_valentine_heart", "mount_hipster_bike", "mount_valentine_pegase",
            "pet_valentine_cupidon", "mount_wooden_plane", "pet_snowman",
            "pet_hipster_penguin", "valentine_letter", "poop", "golden_poop");

    private static final List<String> IGNORED_PREFIXES = List.of(
            "nameplate_", "emote_", "lny_", "xmas_", "pet_", "mount_",
            "fragment_", "key_", "treasure_", "ship_");

    private static volatile List<String> museumCatalog = null;

    private MuseumTracker() {}

    public static void init() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mba-museum-poll");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(MuseumTracker::poll, 0L, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private static void poll() {
        try {
            if (!Utils.isOnMinebox()) return;
            if (MineboxAdditions.INSTANCE == null || MineboxAdditions.INSTANCE.state == null) return;

            Minecraft client = Minecraft.getInstance();
            if (client.getUser() == null) return;
            String username = client.getUser().getName();
            if (username == null || username.isEmpty()) return;

            List<String> catalog = museumCatalog;
            if (catalog == null) {
                catalog = fetchCatalog();
                if (catalog == null) return;
                museumCatalog = catalog;
            }

            Set<String> owned = fetchOwnedIds(username);
            if (owned == null) return;

            List<String> missing = new ArrayList<>();
            for (String id : catalog) {
                if (owned.contains(id)) continue;
                if (IGNORED_IDS.contains(id)) continue;
                if (hasIgnoredPrefix(id)) continue;
                missing.add(id);
            }

            MineboxAdditions.INSTANCE.state.setMissingMuseumItemIds(missing);
        } catch (Exception e) {
            System.out.println("[MuseumTracker] poll failed: " + e.getMessage());
        }
    }

    private static boolean hasIgnoredPrefix(String id) {
        for (String p : IGNORED_PREFIXES) {
            if (id.startsWith(p)) return true;
        }
        return false;
    }

    private static List<String> fetchCatalog() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(MUSEUM_CATALOG_URL)).GET().build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;

            JsonNode root = MAPPER.readTree(res.body());
            if (root == null || !root.isObject()) return null;

            List<String> ids = new ArrayList<>();
            root.forEach(group -> {
                if (group.isArray()) {
                    group.forEach(node -> {
                        if (node.isTextual() && !node.asText().isEmpty()) ids.add(toInGameId(node.asText()));
                    });
                }
            });
            return ids;
        } catch (Exception e) {
            System.out.println("[MuseumTracker] catalog fetch failed: " + e.getMessage());
            return null;
        }
    }

    private static Set<String> fetchOwnedIds(String username) {
        try {
            String url = String.format(USER_DATA_URL, URLEncoder.encode(username, StandardCharsets.UTF_8));
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) return null;

            JsonNode root = MAPPER.readTree(res.body());
            JsonNode payload = root.hasNonNull("data") ? root.get("data") : root;
            JsonNode museum = payload.path("OBJECTIVES").path("museum");

            Set<String> owned = new HashSet<>();
            if (museum.isArray()) {
                museum.forEach(node -> {
                    if (node.isTextual() && !node.asText().isEmpty()) owned.add(node.asText());
                });
            }
            return owned;
        } catch (Exception e) {
            System.out.println("[MuseumTracker] user data fetch failed: " + e.getMessage());
            return null;
        }
    }

    private static String toInGameId(String id) {
        int sep = id.indexOf('_');
        if (sep == -1) return id;
        String prefix = id.substring(0, sep);
        String crop = id.substring(sep + 1);
        if (MATERIAL_PREFIXES.contains(prefix) && CROP_NAMES.contains(crop)) {
            return prefix + "_material-" + crop;
        }
        return id;
    }
}
