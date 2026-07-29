package io.dampen59.mineboxadditions.utils.models;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum Location implements StringRepresentable {
    SPAWN("spawn"),
    HOME("player_islands"),
    KOKOKO("island_tropical"),
    QUADRA_PLAINS("island_plain"),
    BAMBOO_PEAK("island_bamboo"),
    FROSTBITE_FORTRESS("island_snow"),
    SANDWHISPER_DUNES("island_desert"),
    AFK("afk"),
    VILLAGE("player_villages"),
    RAID("raid"),
    UNKNOWN("unknown");

    @NotNull
    private final String id;

    Location(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return id();
    }

    @NotNull
    public static Location from(String id) {
        return Arrays.stream(Location.values())
                .filter(loc -> id.startsWith(loc.id))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
