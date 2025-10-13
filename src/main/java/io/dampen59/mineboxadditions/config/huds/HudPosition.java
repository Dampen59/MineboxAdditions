package io.dampen59.mineboxadditions.config.huds;

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;

@ConfigObject
public class HudPosition {
    @ConfigEntry(id = "x")
    public int x = 0;
    @ConfigEntry(id = "y")
    public int y = 0;

    public HudPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}