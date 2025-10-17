package io.dampen59.mineboxadditions.features.hud.huds;

import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.elements.SpacerElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.elements.TextureElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.HStackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.StackElement;
import io.dampen59.mineboxadditions.features.hud.elements.stack.VStackElement;
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
        String mermaidText = "Unknown";
        if (ShopManager.getMermaid().itemTranslationKey != null && ShopManager.getMermaid().quantity > 0)
            mermaidText = getMermaidText();

        getNamedElement("text", TextElement.class).setText(Text.of(mermaidText));
    }

    private static @NotNull String getMermaidText() {
        String mermaidText;
        if (ShopManager.getMermaid().itemTranslationKeyArgs == null) {
            mermaidText = String.format("%dx %s", ShopManager.getMermaid().quantity, Text.translatable(ShopManager.getMermaid().itemTranslationKey).getString());
        } else {
            mermaidText = String.format("%dx %s", ShopManager.getMermaid().quantity, Text.translatable(ShopManager.getMermaid().itemTranslationKey, Text.translatable(ShopManager.getMermaid().itemTranslationKeyArgs).getString()).getString());
        }
        return mermaidText;
    }
}