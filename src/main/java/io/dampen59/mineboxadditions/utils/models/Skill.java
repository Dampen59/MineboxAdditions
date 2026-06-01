package io.dampen59.mineboxadditions.utils.models;

import net.minecraft.network.chat.Component;

public enum Skill {
    ALCHEMIST,
    BLACKSMITH,
    COOK,
    FARMER,
    FISHERMAN,
    HUNTER,
    JEWELER,
    LUMBERJACK,
    MINER,
    RUNEFORGER,
    SHOEMAKER,
    TAILOR,
    TINKERER;

    public Component getName() {
        return Component.translatable("mbx.skills." + this.name().toLowerCase() + ".name");
    }
}