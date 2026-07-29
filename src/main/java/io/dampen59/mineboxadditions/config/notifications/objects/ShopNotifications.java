package io.dampen59.mineboxadditions.config.notifications.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.network.chat.Component;

@ConfigObject
public class ShopNotifications implements Translatable {
    @ConfigOption.Separator(value = "Mouse")
    @ConfigEntry(id = "mouseToast", translation = "mineboxadditions.config.notifications.shop.mouse.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.mouse.toast.desc")
    public boolean mouseToast = true;

    @ConfigEntry(id = "mouseBell", translation = "mineboxadditions.config.notifications.shop.mouse.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.mouse.bell.desc")
    public boolean mouseBell = true;

    @ConfigOption.Separator(value = "Bakery")
    @ConfigEntry(id = "bakeryToast", translation = "mineboxadditions.config.notifications.shop.bakery.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.bakery.toast.desc")
    public boolean bakeryToast = true;

    @ConfigEntry(id = "bakeryBell", translation = "mineboxadditions.config.notifications.shop.bakery.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.bakery.bell.desc")
    public boolean bakeryBell = true;

    @ConfigOption.Separator(value = "Buckstar")
    @ConfigEntry(id = "buckstarToast", translation = "mineboxadditions.config.notifications.shop.buckstar.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.buckstar.toast.desc")
    public boolean buckstarToast = true;

    @ConfigEntry(id = "buckstarBell", translation = "mineboxadditions.config.notifications.shop.buckstar.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.buckstar.bell.desc")
    public boolean buckstarBell = true;

    @ConfigOption.Separator(value = "Sharkoffe")
    @ConfigEntry(id = "sharkoffeToast", translation = "mineboxadditions.config.notifications.shop.sharkoffe.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.sharkoffe.toast.desc")
    public boolean sharkoffeToast = true;

    @ConfigEntry(id = "sharkoffeBell", translation = "mineboxadditions.config.notifications.shop.sharkoffe.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.sharkoffe.bell.desc")
    public boolean sharkoffeBell = true;

    @ConfigOption.Separator(value = "Reggae Dealer")
    @ConfigEntry(id = "reggaeDealerToast", translation = "mineboxadditions.config.notifications.shop.reggae_dealer.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.reggae_dealer.toast.desc")
    public boolean reggaeDealerToast = true;

    @ConfigEntry(id = "reggaeDealerBell", translation = "mineboxadditions.config.notifications.shop.reggae_dealer.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.reggae_dealer.bell.desc")
    public boolean reggaeDealerBell = true;

    @ConfigOption.Separator(value = "Paintings Seller")
    @ConfigEntry(id = "paintingsSellerToast", translation = "mineboxadditions.config.notifications.shop.paintings_seller.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.paintings_seller.toast.desc")
    public boolean paintingsSellerToast = true;

    @ConfigEntry(id = "paintingsSellerBell", translation = "mineboxadditions.config.notifications.shop.paintings_seller.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.paintings_seller.bell.desc")
    public boolean paintingsSellerBell = true;

    @ConfigOption.Separator(value = "Sushi Seller")
    @ConfigEntry(id = "sushiSellerToast", translation = "mineboxadditions.config.notifications.shop.sushi_seller.toast")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.sushi_seller.toast.desc")
    public boolean sushiSellerToast = true;

    @ConfigEntry(id = "sushiSellerBell", translation = "mineboxadditions.config.notifications.shop.sushi_seller.bell")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.sushi_seller.bell.desc")
    public boolean sushiSellerBell = true;

    @Override
    public String getTranslationKey() {
        return Component.translatable("mineboxadditions.config.notifications.shop").getString();
    }
}
