package io.dampen59.mineboxadditions.hud;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class HudRenderer {
    private final State modState;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public HudRenderer(State modState) {
        this.modState = modState;
        HudRenderCallback.EVENT.register(this::renderHud);
    }

    private void renderHud(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (client == null || client.player == null || client.options.hudHidden) return;

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.displaySettings.displayMermaidRequest) return;

        if (this.modState.getMermaidItemOffer().itemTranslationKey != null && this.modState.getMermaidItemOffer().quantity > 0) {

            String mermaidText = null;

            if (this.modState.getMermaidItemOffer().itemTranslationKeyArgs == null) {
                mermaidText = String.format("Mermaid request: %dx %s", this.modState.getMermaidItemOffer().quantity, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKey).getString());
            } else {
                mermaidText = String.format("Mermaid request: %dx %s", this.modState.getMermaidItemOffer().quantity, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKey, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKeyArgs).getString()).getString());
            }

            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(mermaidText), config.mermaidRequestHudX, config.getMermaidRequestHudY, 0xFFFFFFFF);
        } else {
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal("Mermaid request: Unknown"), config.mermaidRequestHudX, config.getMermaidRequestHudY, 0xFFFFFFFF);
        }


    }

}
