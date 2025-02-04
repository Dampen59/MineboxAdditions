package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;

public class ShopEventManager {

    public ShopEventManager(State modState) {
        new ShopEvent(Shop.BAKERY,
                modState,
                modState::getBakeryAlertSent,
                modState::setBakeryAlertSent,
                modState::getBakeryCurrentItemOffer,
                modState::setBakeryCurrentItemOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getBakeryAlerts);

        new ShopEvent(Shop.BUCKSTAR,
                modState,
                modState::getBuckstarAlertSent,
                modState::setBuckstarAlertSent,
                modState::getBuckstarCurrentItemOffer,
                modState::setBuckstarCurrentItemOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getBuckstarAlerts);

        new ShopEvent(Shop.COCKTAIL,
                modState,
                modState::getCocktailAlertSent,
                modState::setCocktailAlertSent,
                modState::getCocktailCurrentItemOffer,
                modState::setCocktailCurrentItemOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getCocktailAlerts);

        new ShopEvent(Shop.MOUSE,
                modState,
                modState::getMouseAlertSent,
                modState::setMouseAlertSent,
                modState::getMouseCurrentItemOffer,
                modState::setMouseCurrentItemOffer,
                () -> AutoConfig.getConfigHolder(ModConfig.class).getConfig().shopsAlertsSettings.getMouseAlerts);
    }
}
