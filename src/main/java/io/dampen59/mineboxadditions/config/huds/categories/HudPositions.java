package io.dampen59.mineboxadditions.config.huds.categories;

import com.teamresourceful.resourcefulconfig.api.annotations.Category;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption;
import io.dampen59.mineboxadditions.config.huds.HudPosition;

@Category("positions")
@ConfigOption.Hidden
public class HudPositions {
    @ConfigOption.Hidden
    @ConfigEntry(id = "island")
    public static final HudPosition island = new HudPosition(100, 4);

    @ConfigOption.Hidden
    @ConfigEntry(id = "time")
    public static final HudPosition time = new HudPosition(100, 4);

    @ConfigOption.Hidden
    @ConfigEntry(id = "vote")
    public static final HudPosition vote = new HudPosition(120, 4);

    @ConfigOption.Hidden
    @ConfigEntry(id = "shop")
    public static final HudPosition shop = new HudPosition(4, 4);

    @ConfigOption.Hidden
    @ConfigEntry(id = "mermaid")
    public static final HudPosition mermaid = new HudPosition(4, 20);

    @ConfigOption.Hidden
    @ConfigEntry(id = "rain")
    public static final HudPosition rain = new HudPosition(4, 36);

    @ConfigOption.Hidden
    @ConfigEntry(id = "storm")
    public static final HudPosition storm = new HudPosition(68, 36);

    @ConfigOption.Hidden
    @ConfigEntry(id = "fullMoon")
    public static final HudPosition fullMoon = new HudPosition(132, 36);

    @ConfigOption.Hidden
    @ConfigEntry(id = "haversackRate")
    public static final HudPosition haversackRate = new HudPosition(4, 52);

    @ConfigOption.Hidden
    @ConfigEntry(id = "haversackFull")
    public static final HudPosition haversackFull = new HudPosition(4, 68);

    @ConfigOption.Hidden
    @ConfigEntry(id = "itempickup")
    public static final HudPosition itempickup = new HudPosition(-50, 4);
}