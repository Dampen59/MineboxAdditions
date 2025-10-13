package io.dampen59.mineboxadditions.config;

import com.teamresourceful.resourcefulconfig.api.loader.Configurator;
import io.dampen59.mineboxadditions.MineboxAdditions;

public class ConfigManager {
    public static final Configurator configurator = new Configurator(MineboxAdditions.NAMESPACE);

    public static void init() {
        configurator.register(Config.class);
    }

    public static void save() {
        configurator.saveConfig(Config.class);
    }
}