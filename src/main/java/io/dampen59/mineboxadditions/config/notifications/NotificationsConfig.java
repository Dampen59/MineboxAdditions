package io.dampen59.mineboxadditions.config.notifications;

import com.teamresourceful.resourcefulconfig.api.annotations.*;
import io.dampen59.mineboxadditions.config.notifications.objects.ShopNotifications;

@Category(value = "notifications")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.notifications",
        descriptionTranslation = "mineboxadditions.config.notifications.desc"
)
public class NotificationsConfig {
    @ConfigEntry(id = "shop", translation = "mineboxadditions.config.notifications.shop")
    @Comment(value = "", translation = "mineboxadditions.config.notifications.shop.desc")
    public static final ShopNotifications shop = new ShopNotifications();
}
