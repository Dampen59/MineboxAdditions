package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextureElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
import io.dampen59.mineboxadditions.state.State;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MermaidHud extends Hud {
    public MermaidHud() {
        super(
                () -> HudsConfig.mermaid,
                s -> HudsConfig.mermaid = s,
                () -> HudPositions.mermaid.x,
                x -> HudPositions.mermaid.x = x,
                () -> HudPositions.mermaid.y,
                y -> HudPositions.mermaid.y = y);

        ClientTickEvents.END_CLIENT_TICK.register(this::update);
    }

    @Override
    public StackElement init() {
        Identifier texture = Identifier.of("mineboxadditions", "textures/icons/mermaid.png");
        TextElement text = new TextElement(Text.of("1x Bedrock"));

        HStackElement hstack = new HStackElement()
                .add(new SpacerElement(4))
                .add(new TextureElement(texture, 10, 10))
                .add(new SpacerElement(4))
                .add(new VStackElement().add(new SpacerElement(1), text))
                .add(new SpacerElement(4));
        addNamedElement("text", text);

        return new VStackElement().add(new SpacerElement(2), hstack, new SpacerElement(2));
    }

    private void update(MinecraftClient client) {
        State state = MineboxAdditions.INSTANCE.state;

        String mermaidText = "Unknown";
        if (state.getMermaidItemOffer().itemTranslationKey != null && state.getMermaidItemOffer().quantity > 0)
            mermaidText = getMermaidText(state);

        getNamedElement("text", TextElement.class).setText(Text.of(mermaidText));
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