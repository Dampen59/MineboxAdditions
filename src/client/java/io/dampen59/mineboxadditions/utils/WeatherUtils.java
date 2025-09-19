package io.dampen59.mineboxadditions.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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

    private static final String[] TIMES_OF_DAY = {"night", "morning", "afternoon", "evening"};

    public static ForecastResult parseWeatherForecast(int dayIndex, String dialog) {
        String dialogLower = dialog.toLowerCase(Locale.ROOT);

        List<String> glyphs = extractWeatherGlyphs(dialogLower);
        if (glyphs.size() < 4) return null;

        Map<String, WeatherType> forecast = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            forecast.put(TIMES_OF_DAY[i], stringToWeather(glyphs.get(i)));
        }

        return new ForecastResult(dayIndex, forecast);
    }

    public static boolean isFullWeatherMessage(String dialog) {
        return dialog.codePoints()
                .filter(cp -> cp == STORM_CODEPOINT || cp == RAIN_CODEPOINT || cp == CLEAR_CODEPOINT)
                .limit(4)
                .count() >= 4;
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
        json.put("day", result.dayIndex);

        JSONObject forecast = new JSONObject();
        for (Map.Entry<String, WeatherType> entry : result.forecast.entrySet()) {
            forecast.put(entry.getKey(), entry.getValue().name());
        }
        json.put("forecast", forecast);

        return json;
    }

    public static class ForecastResult {
        public final int dayIndex;
        public final Map<String, WeatherType> forecast;

        public ForecastResult(int dayIndex, Map<String, WeatherType> forecast) {
            this.dayIndex = dayIndex;
            this.forecast = forecast;
        }
    }
}
