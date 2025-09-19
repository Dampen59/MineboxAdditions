package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class SkyEvent {
    private final State modState;

    public SkyEvent(State prmModState) {
        this.modState = prmModState;
        onTick();
        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                int moonPhase = client.world.getMoonPhase();

                if (this.modState.getCurrentMoonPhase() != 0) {
                    // TODO : if config.enableFullMoonAlerts
                }

                this.modState.setCurrentMoonPhase(moonPhase);
            }
        });
    }

    private boolean isFullMoon() {
        return this.modState.getCurrentMoonPhase() == 0;
    }

    private void onRenderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (isFullMoon() && config.displaySettings.displayFullMoon) {
            Identifier texture = Identifier.of("mineboxadditions", "textures/gui/moon_phases/full_moon.png");
            drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                    config.fullMoonHudX, config.fullMoonHudY,
                    0, 0,
                    24, 24,
                    24, 24);
        }

        String rainText;
        String stormText;

        if (client.world.isRaining()) {
            rainText = "Next Rain: Now !";
        } else {
            rainText = "Next Rain: " + formatNextEventCountdown(modState.getWeatherState().getRainTimestamps());
        }

        if (client.world.isThundering()) {
            stormText = "Next Storm: Now !";
        } else {
            stormText = "Next Storm: " + formatNextEventCountdown(modState.getWeatherState().getStormTimestamps());
        }

        if (config.displaySettings.displayNextRain) {
            drawContext.drawText(client.textRenderer, Text.literal(rainText), config.rainHudX, config.rainHudY, 0xFFFFFFFF, true);
        }
        if (config.displaySettings.displayNextStorm) {
            drawContext.drawText(client.textRenderer, Text.literal(stormText), config.stormHudX, config.stormHudY, 0xFFFFFFFF, true);
        }
    }

    private String formatNextEventCountdown(List<Integer> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) return "Unknown";

        List<Integer> snapshot = snapshotList(timestamps);
        if (snapshot.isEmpty()) return "Unknown";

        int now = (int) (System.currentTimeMillis() / 1000L);
        Integer next = null;
        for (Integer ts : snapshot) {
            if (ts != null && ts > now && (next == null || ts < next)) {
                next = ts;
            }
        }
        if (next == null) return "Unknown";

        int secondsLeft = next - now;
        int hours = secondsLeft / 3600;
        int minutes = (secondsLeft % 3600) / 60;
        int seconds = secondsLeft % 60;
        return String.format("in %02d:%02d:%02d", hours, minutes, seconds);
    }

    private static List<Integer> snapshotList(List<Integer> src) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return new ArrayList<>(src);
            } catch (ConcurrentModificationException ignored) { }
        }
        List<Integer> fallback = new ArrayList<>();
        try {
            for (Integer i : src) fallback.add(i);
        } catch (Exception ignored) { }
        return fallback;
    }
}
