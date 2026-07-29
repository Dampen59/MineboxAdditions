package io.dampen59.mineboxadditions.config.huds.objects;

import com.teamresourceful.resourcefulconfig.api.annotations.Comment;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigObject;
import com.teamresourceful.resourcefulconfig.api.types.info.Translatable;
import net.minecraft.network.chat.Component;

@ConfigObject
public class InsectsHud implements Translatable {

    @ConfigEntry(id = "enabled", translation = "mineboxadditions.config.huds.insects.enabled")
    @Comment(value = "", translation = "mineboxadditions.config.huds.insects.enabled.desc")
    public boolean enabled = true;

    @ConfigEntry(id = "butterfly_atlas_moth",     translation = "mbx.items.butterfly_atlas_moth.name")
    public boolean butterfly_atlas_moth = true;
    @ConfigEntry(id = "butterfly_birdwing",       translation = "mbx.items.butterfly_birdwing.name")
    public boolean butterfly_birdwing = true;
    @ConfigEntry(id = "butterfly_blue",           translation = "mbx.items.butterfly_blue.name")
    public boolean butterfly_blue = true;
    @ConfigEntry(id = "butterfly_green",          translation = "mbx.items.butterfly_green.name")
    public boolean butterfly_green = true;
    @ConfigEntry(id = "butterfly_night",          translation = "mbx.items.butterfly_night.name")
    public boolean butterfly_night = true;
    @ConfigEntry(id = "butterfly_purple_emperor", translation = "mbx.items.butterfly_purple_emperor.name")
    public boolean butterfly_purple_emperor = true;
    @ConfigEntry(id = "butterfly_sunset_moth",    translation = "mbx.items.butterfly_sunset_moth.name")
    public boolean butterfly_sunset_moth = true;
    @ConfigEntry(id = "butterfly_tiger",          translation = "mbx.items.butterfly_tiger.name")
    public boolean butterfly_tiger = true;
    @ConfigEntry(id = "butterfly_white",          translation = "mbx.items.butterfly_white.name")
    public boolean butterfly_white = true;
    @ConfigEntry(id = "butterfly_yellow",         translation = "mbx.items.butterfly_yellow.name")
    public boolean butterfly_yellow = true;

    @ConfigEntry(id = "insect_ant",               translation = "mbx.items.insect_ant.name")
    public boolean insect_ant = true;
    @ConfigEntry(id = "insect_ant_brown",         translation = "mbx.items.insect_ant_brown.name")
    public boolean insect_ant_brown = true;
    @ConfigEntry(id = "insect_centipede",         translation = "mbx.items.insect_centipede.name")
    public boolean insect_centipede = true;
    @ConfigEntry(id = "insect_cricket",           translation = "mbx.items.insect_cricket.name")
    public boolean insect_cricket = true;
    @ConfigEntry(id = "insect_cyclommatus_stag",  translation = "mbx.items.insect_cyclommatus_stag.name")
    public boolean insect_cyclommatus_stag = true;
    @ConfigEntry(id = "insect_dragonfly_blue",    translation = "mbx.items.insect_dragonfly_blue.name")
    public boolean insect_dragonfly_blue = true;
    @ConfigEntry(id = "insect_dragonfly_green",   translation = "mbx.items.insect_dragonfly_green.name")
    public boolean insect_dragonfly_green = true;
    @ConfigEntry(id = "insect_dragonfly_red",     translation = "mbx.items.insect_dragonfly_red.name")
    public boolean insect_dragonfly_red = true;
    @ConfigEntry(id = "insect_dragonfly_yellow",  translation = "mbx.items.insect_dragonfly_yellow.name")
    public boolean insect_dragonfly_yellow = true;
    @ConfigEntry(id = "insect_dung_beetle",       translation = "mbx.items.insect_dung_beetle.name")
    public boolean insect_dung_beetle = true;
    @ConfigEntry(id = "insect_firefly",           translation = "mbx.items.insect_firefly.name")
    public boolean insect_firefly = true;
    @ConfigEntry(id = "insect_ladybug",           translation = "mbx.items.insect_ladybug.name")
    public boolean insect_ladybug = true;
    @ConfigEntry(id = "insect_locust",            translation = "mbx.items.insect_locust.name")
    public boolean insect_locust = true;
    @ConfigEntry(id = "insect_mantis",            translation = "mbx.items.insect_mantis.name")
    public boolean insect_mantis = true;
    @ConfigEntry(id = "insect_mosquito",          translation = "mbx.items.insect_mosquito.name")
    public boolean insect_mosquito = true;
    @ConfigEntry(id = "insect_scorpion",          translation = "mbx.items.insect_scorpion.name")
    public boolean insect_scorpion = true;
    @ConfigEntry(id = "insect_snail",             translation = "mbx.items.insect_snail.name")
    public boolean insect_snail = true;
    @ConfigEntry(id = "insect_spider",            translation = "mbx.items.insect_spider.name")
    public boolean insect_spider = true;
    @ConfigEntry(id = "insect_stick_insect",      translation = "mbx.items.insect_stick_insect.name")
    public boolean insect_stick_insect = true;
    @ConfigEntry(id = "insect_tarantula",         translation = "mbx.items.insect_tarantula.name")
    public boolean insect_tarantula = true;
    @ConfigEntry(id = "insect_wasp",              translation = "mbx.items.insect_wasp.name")
    public boolean insect_wasp = true;

    public boolean isInsectEnabled(String id) {
        try {
            return (boolean) getClass().getField(id).get(this);
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public String getTranslationKey() {
        return Component.translatable("mineboxadditions.config.huds.insects").getString();
    }
}
