package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
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

    private void onRenderHud(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) return;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        int x = 5;
        int y = 60;

        if (isFullMoon() && config.displaySettings.displayFullMoon) {
            Identifier texture = Identifier.of("mineboxadditions", "textures/gui/moon_phases/full_moon.png");
            drawContext.drawTexture(RenderLayer::getGuiTextured, texture, 14, 5, 0, 0, 24, 24, 24, 24);
            drawContext.drawText(client.textRenderer, Text.translatable("mineboxadditions.strings.full_moon"), 5, 32, 0xFFFFFF, true);
        }

        String rainText = "Next Rain: " + formatNextEventCountdown(modState.getRainTimestamps());
        String stormText = "Next Storm: " + formatNextEventCountdown(modState.getStormTimestamps());

        drawContext.drawText(client.textRenderer, Text.literal(rainText), x, y, 0xFFFFFF, true);
        drawContext.drawText(client.textRenderer, Text.literal(stormText), x, y + 12, 0xFFFFFF, true);
    }

    private String formatNextEventCountdown(List<Integer> timestamps) {
        int currentTime = (int) (System.currentTimeMillis() / 1000);

        return timestamps.stream()
                .filter(ts -> ts > currentTime)
                .min(Comparator.naturalOrder())
                .map(next -> {
                    int secondsLeft = next - currentTime;
                    int hours = secondsLeft / 3600;
                    int minutes = (secondsLeft % 3600) / 60;
                    int seconds = secondsLeft % 60;
                    return String.format("in %02d:%02d:%02d", hours, minutes, seconds);
                })
                .orElse("Unknown");
    }
}
