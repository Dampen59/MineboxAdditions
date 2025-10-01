package io.dampen59.mineboxadditions.config.huds;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo;
import io.dampen59.mineboxadditions.config.huds.categories.FishingDrops;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.objects.HaversackHud;
import io.dampen59.mineboxadditions.config.huds.objects.ItemPickupHud;
import io.dampen59.mineboxadditions.config.huds.objects.ShopHud;

@Category(value = "huds", categories = {
        FishingDrops.class,
        HudPositions.class
})
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.huds",
        descriptionTranslation = "mineboxadditions.config.huds.desc"
)
public class HudsConfig {
    @ConfigEntry(id = "shop", translation = "mineboxadditions.config.huds.shop")
    @Comment(value = "", translation = "mineboxadditions.config.huds.shop.desc")
    public static final ShopHud shop = new ShopHud();

    @ConfigEntry(id = "mermaid", translation = "mineboxadditions.config.huds.mermaid")
    @Comment(value = "", translation = "mineboxadditions.config.huds.mermaid.desc")
    public static boolean mermaid = true;

    @ConfigEntry(id = "rain", translation = "mineboxadditions.config.huds.rain")
    @Comment(value = "", translation = "mineboxadditions.config.huds.rain.desc")
    public static boolean rain = true;

    @ConfigEntry(id = "storm", translation = "mineboxadditions.config.huds.storm")
    @Comment(value = "", translation = "mineboxadditions.config.huds.storm.desc")
    public static boolean storm = true;

    @ConfigEntry(id = "fullmoon", translation = "mineboxadditions.config.huds.fullmoon")
    @Comment(value = "", translation = "mineboxadditions.config.huds.fullmoon.desc")
    public static boolean fullmoon = true;

    @ConfigEntry(id = "haversack", translation = "mineboxadditions.config.huds.haversack")
    @Comment(value = "", translation = "mineboxadditions.config.huds.haversack.desc")
    public static final HaversackHud haversack = new HaversackHud();

    @ConfigEntry(id = "itempickup", translation = "mineboxadditions.config.huds.itempickup")
    @Comment(value = "", translation = "mineboxadditions.config.huds.itempickup.desc")
    public static final ItemPickupHud itempickup = new ItemPickupHud();
}
