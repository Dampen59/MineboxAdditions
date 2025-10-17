package io.dampen59.mineboxadditions.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dampen59.mineboxadditions.utils.models.Harvestable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ApiUtils {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static List<Harvestable> harvestables;

    private static String request(String url) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder().GET().uri(URI.create(url));
        HttpRequest request = builder.build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static <T> List<T> getList(String url, Class<T> clazz) {
        try {
            String json = request(url);
            if (json.isEmpty()) return List.of();
            return GSON.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
        } catch (Exception e) {
            return List.of();
        }
    }

    public static List<Harvestable> getHarvestables() {
        if (harvestables == null)
            harvestables = getList("https://api.minebox.co/files/harvestables", Harvestable.class);
        return harvestables;
    }
}
