package io.dampen59.mineboxadditions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.AutoIsland;
import io.dampen59.mineboxadditions.features.ShinyTracker;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoalDisplay;
import io.dampen59.mineboxadditions.features.harvestable.HarvestableScreen;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.item.ItemTooltip;
import io.dampen59.mineboxadditions.events.*;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.HudEditorScreen;
import io.dampen59.mineboxadditions.features.atlas.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Scheduler;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class MineboxAdditions implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mineboxadditions");
    public static final String NAMESPACE = "mineboxadditions";
    public static MineboxAdditions INSTANCE;

    public State state = null;
    private static KeyMapping openModSettings;
    public static KeyMapping openEditMode;
    public static KeyMapping openHarvestables;
    public static KeyMapping openAtlas;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ConfigManager.init();
        Utils.init();

        this.state = new State();
        SocketManager.init();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            HudManager.INSTANCE.init();
        });

        AutoIsland.init();
        FishingShoalDisplay.init();
        ShinyTracker.init();
        ShopManager.init();
        ItemTooltip.init();

        new SkyEvent();
        new ServerEvents(state);
        new ContainerOpenEvent(state);
        new WorldRendererEvent();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        this.registerKeybinds();

        INSTANCE = this;
    }

    public void tick(Minecraft client) {
        Scheduler.INSTANCE.tick();

        if (openEditMode.consumeClick()) {
            client.setScreen(new HudEditorScreen());
        }
        if (openAtlas.consumeClick()) {
            if (state.getMbxItems() == null || state.getMbxItems().isEmpty()) {
                Utils.displayChatErrorMessage(Component.translatable("mineboxadditions.strings.errors.missing_atlas_data").getString());
                return;
            }
            client.setScreen(new MineboxAtlasScreen());
        }
        if (openHarvestables.consumeClick()) {
            client.setScreen(new HarvestableScreen());
            //client.setScreen(new HarvestableMapScreen());
        }
        if (openModSettings.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(ResourcefulConfigScreen.make(ConfigManager.configurator, Config.class).build());
            }
        }
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("mba")
                .then(ClientCommands.literal("debug")
                    .executes(context -> {
                        Utils.displayChatInfoMessage("=== MineboxAdditions Debug Informations ===");
                        Utils.displayChatInfoMessage("Mod Version: " + Utils.getModVersion());
                        Utils.displayChatInfoMessage("Socket state: " + (SocketManager.getSocket().connected() ? "connected (ID : " + SocketManager.getSocket().id() + ")" : "disconnected"));
                        Utils.displayChatInfoMessage("Rain Data: " + this.state.getWeatherState().getRainTimestamps().stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        Utils.displayChatInfoMessage("Storm Data: " + this.state.getWeatherState().getStormTimestamps().stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        Utils.displayChatInfoMessage("Shiny Length: " + ShinyTracker.getShinyCount());

                        if (ShopManager.getMermaid().itemTranslationKey != null) {
                            Utils.displayChatInfoMessage(String.format(
                                    "Mermaid Data: {%s, %d}",
                                    ShopManager.getMermaid().itemTranslationKey,
                                    ShopManager.getMermaid().quantity
                            ));
                        } else {
                            Utils.displayChatInfoMessage("Mermaid Data: None");
                        }

                        Utils.displayChatInfoMessage("Museum Length: " + this.state.getMissingMuseumItemIds().size());

                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
    }

    private static final KeyMapping.Category MBX_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("mineboxadditions", "mineboxadditions"));

    private void registerKeybinds() {
        openModSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.modSettings.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                MBX_CATEGORY
        ));

        openEditMode = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.hudEditor.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                MBX_CATEGORY
        ));

        openHarvestables = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.harvestables.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                MBX_CATEGORY
        ));

        openAtlas = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.atlas.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                MBX_CATEGORY
        ));

    }
}
