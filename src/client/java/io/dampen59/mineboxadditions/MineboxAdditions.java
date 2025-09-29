package io.dampen59.mineboxadditions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.dampen59.mineboxadditions.audio.AudioManager;
import io.dampen59.mineboxadditions.events.*;
import io.dampen59.mineboxadditions.events.inventory.InventoryEvent;
import io.dampen59.mineboxadditions.events.shop.ShopEventManager;
import io.dampen59.mineboxadditions.gui.AudioDeviceScreen;
import io.dampen59.mineboxadditions.gui.HarvestablesScreen;
import io.dampen59.mineboxadditions.gui.HudEditorScreen;
import io.dampen59.mineboxadditions.gui.MineboxAtlasScreen;
import io.dampen59.mineboxadditions.hud.HudRenderer;
import io.dampen59.mineboxadditions.network.SocketManager;
import io.dampen59.mineboxadditions.state.AudioDeviceState;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.AudioUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class MineboxAdditions implements ClientModInitializer {
    public static MineboxAdditions INSTANCE;
    public static final Logger LOGGER = LoggerFactory.getLogger("mineboxadditions");
    public State state = null;

    private static KeyBinding openModSettings;
    private static KeyBinding openAudioSettings;
    public static KeyBinding openEditMode;
    public static KeyBinding openHarvestables;
    public static KeyBinding openAtlas;

    @Override
    public void onInitializeClient() {
        MineboxAdditionConfig.init();
        this.state = new State();

        new SocketManager(state);
        new ServerEvents(state);
        new ShopEventManager(state);
        new InventoryEvent(state);
        new ContainerOpenEvent(state);
        new TooltipEvent(state);
        new SkyEvent(state);
        new ShinyEvent(state);
        new WorldRendererEvent();
        new AudioManager(state);
        new HudRenderer(state);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        this.registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openAudioSettings.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new AudioDeviceScreen());
            }
            if (openEditMode.wasPressed()) {
                client.setScreen(new HudEditorScreen());
            }
            if (openAtlas.wasPressed()) {
                if (MineboxAdditions.INSTANCE.state.getMbxItems() == null) {
                    Utils.displayChatErrorMessage(Text.translatable("mineboxadditions.strings.errors.missing_atlas_data").getString());
                    return;
                }
                client.setScreen(new MineboxAtlasScreen());
            }
            if (openHarvestables.wasPressed()) {
                client.setScreen(new HarvestablesScreen());
            }
            if (openModSettings.wasPressed()) {
                if (client.currentScreen == null) {
                    Screen configScreen = AutoConfig.getConfigScreen(MineboxAdditionConfig.class, client.currentScreen).get();
                    client.setScreen(configScreen);
                }
            }
        });

        AudioDeviceState.micGainDb = MineboxAdditionConfig.get().micGainDb;
        AudioDeviceState.selectedInput = AudioUtils.getMixerByName(MineboxAdditionConfig.get().selectedMicName, true);
        AudioDeviceState.selectedOutput = AudioUtils.getMixerByName(MineboxAdditionConfig.get().selectedSpeakerName, false);

        INSTANCE = this;
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("create")
                                .executes(context -> {
                                    this.state.getSocket().emit("C2SCreateAudioRoom");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("join")
                                .then(ClientCommandManager.argument("code", StringArgumentType.string())
                                        .executes(context -> {
                                            String code = StringArgumentType.getString(context, "code");
                                            this.state.getSocket().emit("C2SJoinAudioRoom", code);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("proximity")
                                .executes(context -> {
                                    this.state.getSocket().emit("C2SToggleProximityAudio");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("leave")
                                .executes(context -> {
                                    this.state.getSocket().emit("C2SLeaveAudioRoom");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("debug")
                    .executes(context -> {
                        Utils.displayChatInfoMessage("=== MineboxAdditions Debug Informations ===");
                        Utils.displayChatInfoMessage("Mod Version: " + Utils.getModVersion());
                        Utils.displayChatInfoMessage("Socket state: " + (this.state.getSocket().connected() ? "connected (ID : " + this.state.getSocket().id() + ")" : "disconnected"));
                        Utils.displayChatInfoMessage("Rain Data: " + this.state.getWeatherState().getRainTimestamps().stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        Utils.displayChatInfoMessage("Storm Data: " + this.state.getWeatherState().getStormTimestamps().stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        Utils.displayChatInfoMessage("Shiny Length: " + this.state.getMbxShiniesUuids().size());

                        if (this.state.getMermaidItemOffer().itemTranslationKey != null) {
                            Utils.displayChatInfoMessage(String.format(
                                    "Mermaid Data: {%s, %d}",
                                    this.state.getMermaidItemOffer().itemTranslationKey,
                                    this.state.getMermaidItemOffer().quantity
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

    private void registerKeybinds() {
        openModSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.modSettings.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "MineboxAdditions"
        ));

        openAudioSettings = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.audioSettings.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                "MineboxAdditions"
        ));

        openEditMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.hudEditor.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "MineboxAdditions"
        ));

        openHarvestables = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.harvestables.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "MineboxAdditions"
        ));

        openAtlas = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.atlas.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "MineboxAdditions"
        ));

    }

}
