package io.dampen59.mineboxadditions.utils.models;

import net.minecraft.text.Text;

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

    public Text getName() {
        return Text.translatable("mbx.skills." + this.name().toLowerCase() + ".name");
    }
}