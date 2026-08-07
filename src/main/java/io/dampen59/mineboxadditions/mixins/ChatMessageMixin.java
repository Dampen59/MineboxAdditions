package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.utils.WeatherUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.chat.Component;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ChatMessageMixin {

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void mbx$onGameMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) throws JSONException {
        Component messageText = packet.content();
        String rawMessage = messageText.getString();

        if (!WeatherUtils.isFullWeatherMessage(rawMessage)) return;

        String lastCommand = MineboxAdditions.INSTANCE.state.getLastSentCommand();
        if (lastCommand == null) return;

        if (lastCommand.startsWith("/prediction") || lastCommand.startsWith("/meteo") || lastCommand.startsWith("/forecast") || lastCommand.startsWith("/weather")) {
            int dayIndex = 0;
            String[] weatherArgs = lastCommand.split(" ");

            if (weatherArgs.length > 1) {
                if (!Utils.isInteger(weatherArgs[1])) return;
                dayIndex = Integer.parseInt(weatherArgs[1]);
                if (dayIndex < 0 || dayIndex > 7) return;
            }

            WeatherUtils.ForecastResult forecastData = WeatherUtils.parseWeatherForecast(dayIndex, rawMessage);

            if (forecastData != null) {
                try {
                    JSONObject forecastDataJson = WeatherUtils.parseWeatherForecastJson(forecastData);
                    SocketManager.getSocket().emit("C2SWeatherForecast", forecastDataJson);
                } catch (JSONException e) {
                    System.out.println("Failed to serialize weather forecast data : " + e.getMessage());
                }
            }
        }
    }

}

