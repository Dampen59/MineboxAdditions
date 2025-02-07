package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import java.util.List;
import java.util.Map;

public class ContainerOpenEvent {
    private final State modState;
    private final Map<String, List<String>> shopOffers = Map.of(
            "Mouse", List.of("mbx.items.emmental_cheese.name", "mbx.items.cheddar_cheese.name"),
            "Buckstar", List.of("mbx.items.coffee_gray.name", "mbx.items.coffee_green.name", "mbx.items.coffee_white.name", "mbx.items.coffee_yellow.name"),
            "Bakery", List.of("mbx.items.flour.name", "mbx.items.baking_powder.name", "mbx.items.baguette.name", "mbx.items.croissant.name", "mbx.items.blue_macaron.name", "mbx.items.green_macaron.name", "mbx.items.orange_macaron.name", "mbx.items.yellow_macaron.name"),
            "Cocktail", List.of("mbx.items.blue_cocktail.name", "mbx.items.orange_cocktail.name", "mbx.items.red_cocktail.name", "mbx.items.yellow_cocktail.name", "mbx.items.lemon.name", "mbx.items.avocado.name")
    );

    public ContainerOpenEvent(State modState) {
        this.modState = modState;
        ScreenEvents.AFTER_INIT.register(this::onContainerOpened);
    }

    private void onContainerOpened(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof GenericContainerScreen containerScreen)) {
            return;
        }
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        int slotsCount = containerScreen.getScreenHandler().slots.size();
        if (slotsCount > 45) {
            return;
        }
        Text containerTitle = containerScreen.getTitle();
        String translationKey = extractTranslationKey(containerTitle);
        if (translationKey == null) {
            return;
        }
        shopOffers.forEach((shop, keys) -> {
            if (keys.contains(translationKey) && config.networkFeatures.sendShopsAlerts) {
                modState.getSocket().emit("C2SShopOfferEvent", shop, translationKey);
            }
        });
    }

    private String extractTranslationKey(Text text) {
        if (text.getContent() instanceof TranslatableTextContent translatable) {
            return translatable.getKey();
        }
        for (Text sibling : text.getSiblings()) {
            String key = extractTranslationKey(sibling);
            if (key != null) {
                return key;
            }
        }
        return null;
    }
}
