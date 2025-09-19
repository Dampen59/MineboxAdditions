package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.OfferState;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;

public class ShopEventManager {

    public ShopEventManager(State modState) {
        OfferState offers = modState.getOfferState();

        registerShopEvent(
                Shop.BAKERY,
                modState,
                offers::isBakeryAlertSent,
                offers::setBakeryAlertSent,
                offers::getBakeryOffer,
                offers::setBakeryOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getBakeryAlerts
        );

        registerShopEvent(
                Shop.BUCKSTAR,
                modState,
                offers::isBuckstarAlertSent,
                offers::setBuckstarAlertSent,
                offers::getBuckstarOffer,
                offers::setBuckstarOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getBuckstarAlerts
        );

        registerShopEvent(
                Shop.COCKTAIL,
                modState,
                offers::isCocktailAlertSent,
                offers::setCocktailAlertSent,
                offers::getCocktailOffer,
                offers::setCocktailOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getCocktailAlerts
        );

        registerShopEvent(
                Shop.MOUSE,
                modState,
                offers::isMouseAlertSent,
                offers::setMouseAlertSent,
                offers::getMouseOffer,
                offers::setMouseOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getMouseAlerts
        );
    }

    private void registerShopEvent(
            Shop shop,
            State modState,
            java.util.function.BooleanSupplier isAlertSent,
            java.util.function.Consumer<Boolean> setAlertSent,
            java.util.function.Supplier<String> getOffer,
            java.util.function.Consumer<String> setOffer,
            java.util.function.BooleanSupplier isConfigEnabled
    ) {
        new ShopEvent(shop, modState, isAlertSent, setAlertSent, getOffer, setOffer, isConfigEnabled);
    }
}