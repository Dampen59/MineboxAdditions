package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.MineboxAdditionConfig;
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
                () -> MineboxAdditionConfig.get().shopsAlertsSettings.getBakeryAlerts
        );

        registerShopEvent(
                Shop.BUCKSTAR,
                modState,
                offers::isBuckstarAlertSent,
                offers::setBuckstarAlertSent,
                offers::getBuckstarOffer,
                offers::setBuckstarOffer,
                () -> MineboxAdditionConfig.get().shopsAlertsSettings.getBuckstarAlerts
        );

        registerShopEvent(
                Shop.COCKTAIL,
                modState,
                offers::isCocktailAlertSent,
                offers::setCocktailAlertSent,
                offers::getCocktailOffer,
                offers::setCocktailOffer,
                () -> MineboxAdditionConfig.get().shopsAlertsSettings.getCocktailAlerts
        );

        registerShopEvent(
                Shop.MOUSE,
                modState,
                offers::isMouseAlertSent,
                offers::setMouseAlertSent,
                offers::getMouseOffer,
                offers::setMouseOffer,
                () -> MineboxAdditionConfig.get().shopsAlertsSettings.getMouseAlerts
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