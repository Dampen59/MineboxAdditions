package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.utils.WeatherUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private String currentMessage = null;

    @Inject(method = "setOverlayMessage", at = @At("TAIL"))
    private void onActionBarMessage(Text message, boolean overlay, CallbackInfo ci) {
        MinecraftClient.getInstance().execute(() -> {

            if (Objects.equals(this.currentMessage, message.getString())) return;

            this.currentMessage = message.getString();

            // 끳 = Luxs
            // 끱 = Gems
            if (this.currentMessage.contains("끳") && this.currentMessage.contains("끱")) {
                MineboxAdditionsClient.INSTANCE.modState.setChatLang(this.currentMessage);
                return;
            }

            if(WeatherUtils.isFullWeatherMessage(this.currentMessage)) {
                WeatherUtils.ForecastResult parsedData = WeatherUtils.parseWeatherForecast(this.currentMessage);
                if (parsedData != null) {
                    try {
                        JSONObject jsonWeatherData = WeatherUtils.parseWeatherForecastJson(parsedData);
                        MineboxAdditionsClient.INSTANCE.modState.getSocket().emit("C2SWeatherForecastData", jsonWeatherData);
                    } catch (JSONException e) {
                        System.out.println("Failed to serialize weather forecast data : " + e.getMessage());
                    }
                }
            }

        });
    }
}