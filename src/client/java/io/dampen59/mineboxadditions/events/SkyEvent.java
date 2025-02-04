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

public class SkyEvent {
    private State modState = null;

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
                    // Then: display Toast alert
                }

                this.modState.setCurrentMoonPhase(moonPhase);
            }
        });
    }

    private boolean isFullMoon() {
        return this.modState.getCurrentMoonPhase() == 0;
    }

    private void onRenderHud(DrawContext drawContext, RenderTickCounter renderTickCounter) {

        if (MinecraftClient.getInstance().options.hudHidden) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (isFullMoon() && config.displaySettings.displayFullMoon) {
            Identifier texture = Identifier.of("mineboxadditions", "textures/gui/moon_phases/full_moon.png");
            drawContext.drawTexture(RenderLayer::getGuiTextured, texture, 14, 5, 0, 0, 24, 24, 24, 24);
            drawContext.drawText(client.textRenderer, Text.translatable("mineboxadditions.strings.full_moon"), 5, 32, 0xFFFFFF, true);
        }

    }

}