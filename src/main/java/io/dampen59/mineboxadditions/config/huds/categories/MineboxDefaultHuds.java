package io.dampen59.mineboxadditions.config.huds.categories;

import com.teamresourceful.resourcefulconfig.api.annotations.*;

@Category("mineboxdefaulthuds")
@ConfigInfo(
        titleTranslation = "mineboxadditions.config.huds.mineboxdefault",
        descriptionTranslation = "mineboxadditions.config.huds.mineboxdefault.desc"
)
public class MineboxDefaultHuds {
    @ConfigEntry(id = "island", translation = "mineboxadditions.config.huds.island")
    @Comment(value = "", translation = "mineboxadditions.config.huds.island.desc")
    public static boolean island = false;

    @ConfigEntry(id = "time", translation = "mineboxadditions.config.huds.time")
    @Comment(value = "", translation = "mineboxadditions.config.huds.time.desc")
    public static boolean time = false;

    @ConfigEntry(id = "vote", translation = "mineboxadditions.config.huds.vote")
    @Comment(value = "", translation = "mineboxadditions.config.huds.vote.desc")
    public static boolean vote = false;

    @ConfigEntry(id = "keyfragment", translation = "mineboxadditions.config.huds.keyfragment")
    @Comment(value = "", translation = "mineboxadditions.config.huds.keyfragment.desc")
    public static boolean keyfragment = false;

    @ConfigEntry(id = "statspoints", translation = "mineboxadditions.config.huds.statspoints")
    @Comment(value = "", translation = "mineboxadditions.config.huds.statspoints.desc")
    public static boolean statspoints = false;

    @ConfigEntry(id = "freeitem", translation = "mineboxadditions.config.huds.freeitem")
    @Comment(value = "", translation = "mineboxadditions.config.huds.freeitem.desc")
    public static boolean freeitem = false;
}
