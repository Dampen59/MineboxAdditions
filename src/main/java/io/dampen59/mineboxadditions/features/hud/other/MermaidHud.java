package io.dampen59.mineboxadditions.features.hud.other;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.state.State;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MermaidHud extends Hud {
    public MermaidHud() {
        super(
                () -> HudsConfig.mermaid,
                s -> HudsConfig.mermaid = s,
                () -> HudPositions.mermaid.x,
                () -> HudPositions.mermaid.y,
                x -> HudPositions.mermaid.x = x,
                y -> HudPositions.mermaid.y = y,
                "mermaid", Text.of("1x Bedrock"));
    }

    public static void render(DrawContext context) {
        Hud hud = HudManager.INSTANCE.getHud(Hud.Type.MERMAID_OFFER);
        if (!hud.getState()) return;
        State state = MineboxAdditions.INSTANCE.state;

        String mermaidText = "Unknown";
        if (state.getMermaidItemOffer().itemTranslationKey != null && state.getMermaidItemOffer().quantity > 0)
            mermaidText = getMermaidText(state);

        hud.setText(Text.of(mermaidText));
        hud.draw(context);
    }

    private static @NotNull String getMermaidText(State state) {
        String mermaidText;
        if (state.getMermaidItemOffer().itemTranslationKeyArgs == null) {
            mermaidText = String.format("%dx %s", state.getMermaidItemOffer().quantity, Text.translatable(state.getMermaidItemOffer().itemTranslationKey).getString());
        } else {
            mermaidText = String.format("%dx %s", state.getMermaidItemOffer().quantity, Text.translatable(state.getMermaidItemOffer().itemTranslationKey, Text.translatable(state.getMermaidItemOffer().itemTranslationKeyArgs).getString()).getString());
        }
        return mermaidText;
    }
}