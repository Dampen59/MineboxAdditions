package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.state.OfferState;
import io.dampen59.mineboxadditions.state.State;

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
                () -> HudsConfig.shop.bakery
        );

        registerShopEvent(
                Shop.BUCKSTAR,
                modState,
                offers::isBuckstarAlertSent,
                offers::setBuckstarAlertSent,
                offers::getBuckstarOffer,
                offers::setBuckstarOffer,
                () -> HudsConfig.shop.buckstar
        );

        registerShopEvent(
                Shop.COCKTAIL,
                modState,
                offers::isCocktailAlertSent,
                offers::setCocktailAlertSent,
                offers::getCocktailOffer,
                offers::setCocktailOffer,
                () -> HudsConfig.shop.sharkoffe
        );

        registerShopEvent(
                Shop.MOUSE,
                modState,
                offers::isMouseAlertSent,
                offers::setMouseAlertSent,
                offers::getMouseOffer,
                offers::setMouseOffer,
                () -> HudsConfig.shop.mouse
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