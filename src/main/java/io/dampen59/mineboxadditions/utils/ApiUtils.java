package io.dampen59.mineboxadditions.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.features.bestiary.BestiaryEntry;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoal;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoalDisplay;
import io.dampen59.mineboxadditions.features.harvestable.Harvestable;
import io.dampen59.mineboxadditions.features.item.Insect;
import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.state.State;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApiUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
    private static final String BASE_URL = "https://mineboxadditions.bartier.me";

    private static String request(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        return HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static <T> List<T> fetchList(String path, Class<T> clazz) {
        try {
            String json = request(BASE_URL + path);
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            System.out.println("[ApiUtils] Failed " + path + ": " + e.getMessage());
            return List.of();
        }
    }

    public static void fetchAll(State state) {
        async(() -> state.setMbxItems(fetchList("/items", MineboxItem.class)));
        async(() -> state.setMbxBestiary(fetchList("/bestiary", BestiaryEntry.class)));
        async(() -> state.setInsects(fetchList("/insects", Insect.class)));
        async(() -> {
            try {
                String json = request(BASE_URL + "/harvestables");
                Map<String, List<Harvestable>> map = MAPPER.readValue(json, new TypeReference<>() {});
                map.forEach(state::addMineboxHarvestables);
            } catch (Exception e) {
                System.out.println("[ApiUtils] Failed /harvestables: " + e.getMessage());
            }
        });
        async(() -> FishingShoalDisplay.loadFromApi(fetchList("/fishables", FishingShoal.Item.class)));
        async(() -> state.setAuctionCatalogIds(new HashSet<>(fetchList("/auctionCatalog", String.class))));
    }

    private static void async(Runnable r) {
        new Thread(r).start();
    }
}
