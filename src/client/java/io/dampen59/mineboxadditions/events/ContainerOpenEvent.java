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

import java.util.Arrays;
import java.util.List;

public class ContainerOpenEvent {

    private List<String> mouseItemsOffers = Arrays.asList("mbx.items.emmental_cheese.name", "mbx.items.cheddar_cheese.name");
    private List<String> buckstarItemsOffers = Arrays.asList("mbx.items.coffee_gray.name", "mbx.items.coffee_green.name", "mbx.items.coffee_white.name", "mbx.items.coffee_yellow.name");
    private List<String> bakeryItemsOffers = Arrays.asList("mbx.items.flour.name", "mbx.items.baking_powder.name", "mbx.items.baguette.name", "mbx.items.croissant.name", "mbx.items.blue_macaron.name", "mbx.items.green_macaron.name", "mbx.items.orange_macaron.name", "mbx.items.yellow_macaron.name");
    private List<String> cocktailItemsOffer = Arrays.asList("mbx.items.blue_cocktail.name", "mbx.items.orange_cocktail.name", "mbx.items.red_cocktail.name", "mbx.items.yellow_cocktail.name", "mbx.items.lemon.name", "mbx.items.avocado.name");

    private State modState = null;
    public ContainerOpenEvent(State prmModState) {
        this.modState = prmModState;
        ScreenEvents.AFTER_INIT.register(this::onContainerOpened);
    }

    private void onContainerOpened(MinecraftClient minecraftClient, Screen screen, int i, int i1) {

        if (screen instanceof GenericContainerScreen) {

            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            GenericContainerScreen containerScreen = (GenericContainerScreen) screen;
            int slotsCount = ((GenericContainerScreen) screen).getScreenHandler().slots.size();

            if (slotsCount > 45) return;

            Text containerTitle = containerScreen.getTitle();
            String currentItemOffer = extractTranslationKey(containerTitle);

            if (currentItemOffer == null) return;

            if (mouseItemsOffers.contains(currentItemOffer)) {
                if (config.networkFeatures.sendShopsAlerts) this.modState.getSocket().emit("C2SShopOfferEvent", "Mouse", currentItemOffer);
            }

            if (bakeryItemsOffers.contains(currentItemOffer)) {
                if (config.networkFeatures.sendShopsAlerts) this.modState.getSocket().emit("C2SShopOfferEvent", "Bakery", currentItemOffer);
            }

            if (buckstarItemsOffers.contains(currentItemOffer)) {
                if (config.networkFeatures.sendShopsAlerts) this.modState.getSocket().emit("C2SShopOfferEvent", "Buckstar", currentItemOffer);
            }

            if (cocktailItemsOffer.contains(currentItemOffer)) {
                if (config.networkFeatures.sendShopsAlerts) this.modState.getSocket().emit("C2SShopOfferEvent", "Cocktail", currentItemOffer);
            }

        }
    }

    private String extractTranslationKey(Text text) {

        if (text.getContent() instanceof TranslatableTextContent) {
            TranslatableTextContent translatableContent = (TranslatableTextContent) text.getContent();
            return translatableContent.getKey();
        }

        for (Text sibling : text.getSiblings()) {
            String result = extractTranslationKey(sibling);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
