package io.dampen59.mineboxadditions.hud;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionConfig;
import io.dampen59.mineboxadditions.state.HUDState;
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

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (client == null || client.player == null || client.options.hudHidden) return;

        MineboxAdditionConfig config = AutoConfig.getConfigHolder(MineboxAdditionConfig.class).getConfig();
        HUDState hudState = MineboxAdditions.INSTANCE.modState.getHUDState();
        if (!config.displaySettings.displayMermaidRequest) return;

        if (this.modState.getMermaidItemOffer().itemTranslationKey != null && this.modState.getMermaidItemOffer().quantity > 0) {

            String mermaidText = null;

            if (this.modState.getMermaidItemOffer().itemTranslationKeyArgs == null) {
                mermaidText = String.format("%dx %s", this.modState.getMermaidItemOffer().quantity, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKey).getString());
            } else {
                mermaidText = String.format("%dx %s", this.modState.getMermaidItemOffer().quantity, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKey, Text.translatable(this.modState.getMermaidItemOffer().itemTranslationKeyArgs).getString()).getString());
            }

            Hud hud = hudState.getHud(Hud.Type.MERMAID_OFFER);
            hud.setText(Text.of(mermaidText));
            hud.draw(context);
        } else {
            Hud hud = hudState.getHud(Hud.Type.MERMAID_OFFER);
            hud.setText(Text.of("Unknown"));
            hud.draw(context);
        }


    }

}
