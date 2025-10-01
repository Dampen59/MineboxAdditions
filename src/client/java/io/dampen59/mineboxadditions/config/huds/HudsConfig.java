package io.dampen59.mineboxadditions.config.huds;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import io.dampen59.mineboxadditions.config.huds.categories.FishingDrops;
import io.dampen59.mineboxadditions.config.huds.categories.HudPositions;
import io.dampen59.mineboxadditions.config.huds.objects.HaversackHud;
import io.dampen59.mineboxadditions.config.huds.objects.ItemPickupHud;
import io.dampen59.mineboxadditions.config.huds.objects.ShopHud;

@Category(value = "huds", categories = {
        FishingDrops.class,
        HudPositions.class
})
public class HudsConfig {
    @ConfigEntry(id = "shop")
    @Comment(value = "")
    public static final ShopHud shop = new ShopHud();

    @ConfigEntry(id = "mermaid")
    @Comment(value = "")
    public static boolean mermaid = true;

    @ConfigEntry(id = "rain")
    @Comment(value = "")
    public static boolean rain = true;

    @ConfigEntry(id = "storm")
    @Comment(value = "")
    public static boolean storm = true;

    @ConfigEntry(id = "fullMoon")
    @Comment(value = "")
    public static boolean fullMoon = true;

    @ConfigEntry(id = "haversack")
    @Comment(value = "")
    public static final HaversackHud haversack = new HaversackHud();

    @ConfigEntry(id = "itempickup")
    @Comment(value = "")
    public static final ItemPickupHud itempickup = new ItemPickupHud();
}
