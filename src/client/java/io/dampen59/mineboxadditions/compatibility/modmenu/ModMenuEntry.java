package io.dampen59.mineboxadditions.compatibility.modmenu;

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.dampen59.mineboxadditions.MineboxAdditions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
//        return parent -> AutoConfig.getConfigScreen(MineboxAdditionConfig.class, parent).get();
        return parent -> ResourcefulConfigScreen.getFactory(MineboxAdditions.NAMESPACE).apply(parent);
    }
}