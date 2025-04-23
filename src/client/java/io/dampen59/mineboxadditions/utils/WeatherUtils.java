package io.dampen59.mineboxadditions.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class WeatherUtils {
    public enum WeatherType {
        CLEAR, RAIN, STORM, UNKNOWN
    }

    private static final int CLEAR_CODEPOINT = 0x1000CE;
    private static final int RAIN_CODEPOINT  = 0x1000CF;
    private static final int STORM_CODEPOINT = 0x1000D0;

    private static final String[] TIMES_OF_DAY = {"morning", "afternoon", "evening", "night"};

    private static final Map<Integer, List<String>> DAY_IDENTIFIERS = new LinkedHashMap<>() {{
        put(1, List.of(
                "the weather today", "today's weather", "the forecast for today"
        ));
        put(2, List.of(
                "the weather tomorrow", "tomorrow's weather will", "the forecast for tomorrow"
        ));
        put(3, List.of(
                "the forecast for the day after tomorrow", "the day after tomorrow's"
        ));
        put(4, List.of(
                "three days"
        ));
    }};

    public static ForecastResult parseWeatherForecast(String dialog) {
        dialog = dialog.toLowerCase(Locale.ROOT);
        int day = detectDay(dialog);
        if (day == -1) return null;

        List<String> glyphs = extractWeatherGlyphs(dialog);
        if (glyphs.size() < 4) return null;

        Map<String, WeatherType> forecast = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            forecast.put(TIMES_OF_DAY[i], stringToWeather(glyphs.get(i)));
        }

        return new ForecastResult(day, forecast);
    }

    public static boolean isFullWeatherMessage(String dialog) {
        return dialog.codePoints()
                .filter(cp -> cp == STORM_CODEPOINT || cp == RAIN_CODEPOINT || cp == CLEAR_CODEPOINT)
                .limit(4)
                .count() >= 4;
    }

    private static int detectDay(String dialog) {
        for (Map.Entry<Integer, List<String>> entry : DAY_IDENTIFIERS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (dialog.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    private static List<String> extractWeatherGlyphs(String dialog) {
        List<String> glyphs = new ArrayList<>();
        dialog.codePoints().forEach(cp -> {
            if (cp == STORM_CODEPOINT || cp == RAIN_CODEPOINT || cp == CLEAR_CODEPOINT) {
                String glyph = new String(Character.toChars(cp));
                glyphs.add(glyph);
            }
        });
        return glyphs;
    }

    private static WeatherType stringToWeather(String glyph) {
        int cp = glyph.codePointAt(0);
        return switch (cp) {
            case STORM_CODEPOINT -> WeatherType.STORM;
            case RAIN_CODEPOINT -> WeatherType.RAIN;
            case CLEAR_CODEPOINT -> WeatherType.CLEAR;
            default -> WeatherType.UNKNOWN;
        };
    }

    public static JSONObject parseWeatherForecastJson(ForecastResult result) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("day", result.dayNumber);

        JSONObject forecast = new JSONObject();
        for (Map.Entry<String, WeatherType> entry : result.forecast.entrySet()) {
            forecast.put(entry.getKey(), entry.getValue().name());
        }
        json.put("forecast", forecast);

        return json;
    }

    public static class ForecastResult {
        public final int dayNumber;
        public final Map<String, WeatherType> forecast;

        public ForecastResult(int dayNumber, Map<String, WeatherType> forecast) {
            this.dayNumber = dayNumber;
            this.forecast = forecast;
        }
    }
}
