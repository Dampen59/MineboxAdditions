package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;

public class ShopEventManager {

    public ShopEventManager(State modState) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        new ShopEvent(Shop.BAKERY,
                modState,
                modState::getBakeryAlertSent,
                modState::setBakeryAlertSent,
                modState::getBakeryCurrentItemOffer,
                modState::setBakeryCurrentItemOffer,
                () -> config.shopsAlertsSettings.getBakeryAlerts);

        new ShopEvent(Shop.BUCKSTAR,
                modState,
                modState::getBuckstarAlertSent,
                modState::setBuckstarAlertSent,
                modState::getBuckstarCurrentItemOffer,
                modState::setBuckstarCurrentItemOffer,
                () -> config.shopsAlertsSettings.getBuckstarAlerts);

        new ShopEvent(Shop.COCKTAIL,
                modState,
                modState::getCocktailAlertSent,
                modState::setCocktailAlertSent,
                modState::getCocktailCurrentItemOffer,
                modState::setCocktailCurrentItemOffer,
                () -> config.shopsAlertsSettings.getCocktailAlerts);

        new ShopEvent(Shop.MOUSE,
                modState,
                modState::getMouseAlertSent,
                modState::setMouseAlertSent,
                modState::getMouseCurrentItemOffer,
                modState::setMouseCurrentItemOffer,
                () -> config.shopsAlertsSettings.getMouseAlerts);
    }
}
