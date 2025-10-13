package io.dampen59.mineboxadditions.config;

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColor;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigColorValue;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigInfo;
import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink;
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue;

public class ConfigInfoProvider implements ResourcefulConfigInfo {
    @Override
    public TranslatableValue title() {
        return new TranslatableValue("MineboxAdditions");
    }

    @Override
    public TranslatableValue description() {
        return new TranslatableValue("", "mineboxadditions.config.info.desc");
    }

    @Override
    public ResourcefulConfigLink[] links() {
        return new ResourcefulConfigLink[] {
                ResourcefulConfigLink.create(
                        "https://modrinth.com/mod/mineboxadditions",
                        "modrinth",
                        new TranslatableValue("Modrinth", "mineboxadditions.config.info.modrinth")
                ),
                ResourcefulConfigLink.create(
                        "https://github.com/Dampen59/MineboxAdditions",
                        "code",
                        new TranslatableValue("GitHub", "mineboxadditions.config.info.github")
                )
        };
    }

    @Override
    public String icon() {
        return "skull";
    }

    @Override
    public ResourcefulConfigColor color() {
        return ResourcefulConfigColorValue.create("#FFFFFF");
    }

    @Override
    public boolean isHidden() {
        return false;
    }
}
