package io.dampen59.mineboxadditions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.dampen59.mineboxadditions.audio.AudioManager;
import io.dampen59.mineboxadditions.events.*;
import io.dampen59.mineboxadditions.events.inventory.InventoryEvent;
import io.dampen59.mineboxadditions.events.shop.ShopEventManager;
import io.dampen59.mineboxadditions.gui.AudioDeviceScreen;
import io.dampen59.mineboxadditions.gui.HudEditorScreen;
import io.dampen59.mineboxadditions.minebox.EntitiesOptimizer;
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
import org.lwjgl.glfw.GLFW;

public class MineboxAdditionsClient implements ClientModInitializer {

    public final State modState = new State();
    public static MineboxAdditionsClient INSTANCE;

    private static KeyBinding openModSettings;
    private static KeyBinding openAudioSettings;
    public static KeyBinding openEditMode;
    public static KeyBinding togglePerformanceMode;

    public ModConfig config = null;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        this.config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        new SocketManager(modState);
        new ServerEvents(modState);
        new ShopEventManager(modState);
        new InventoryEvent(modState);
        new ContainerOpenEvent(modState);
        new TooltipEvent(modState);
        new SkyEvent(modState);
        new ShinyEvent(modState);
        new WorldRendererEvent();
        new AudioManager(modState);
        new EntitiesOptimizer(modState);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        this.registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openAudioSettings.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new AudioDeviceScreen());
            }
            if (openEditMode.wasPressed()) {
                client.setScreen(new HudEditorScreen(AutoConfig.getConfigHolder(ModConfig.class).getConfig()));
            }
            if (togglePerformanceMode.wasPressed()) {
                if (modState.getPerformanceMode()) {
                    modState.setPerformanceMode(false);
                    this.modState.getUnrenderedEntities().clear();
                    this.modState.getPerformanceModeDebugArmorstands().clear();
                    Utils.displayChatInfoMessage("Performance mode had been disabled.");
                } else {
                    modState.setPerformanceMode(true);
                    Utils.displayChatInfoMessage("Performance mode had been enabled.");
                }
            }
            if (openModSettings.wasPressed()) {
                if (client.currentScreen == null) {
                    Screen configScreen = AutoConfig.getConfigScreen(ModConfig.class, client.currentScreen).get();
                    client.setScreen(configScreen);
                }
            }
        });

        AudioDeviceState.micGainDb = config.micGainDb;
        AudioDeviceState.selectedInput = AudioUtils.getMixerByName(config.selectedMicName, true);
        AudioDeviceState.selectedOutput = AudioUtils.getMixerByName(config.selectedSpeakerName, false);

        INSTANCE = this;
    }
    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("create")
                                .executes(context -> {
                                    this.modState.getSocket().emit("C2SCreateAudioRoom");
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
                                            this.modState.getSocket().emit("C2SJoinAudioRoom", code);
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
                                    this.modState.getSocket().emit("C2SToggleProximityAudio");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );

        dispatcher.register(ClientCommandManager.literal("mba")
                .then(ClientCommandManager.literal("vc")
                        .then(ClientCommandManager.literal("leave")
                                .executes(context -> {
                                    this.modState.getSocket().emit("C2SLeaveAudioRoom");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
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

        togglePerformanceMode = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "mineboxadditions.strings.keybinds.performanceMode.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "MineboxAdditions"
        ));
    }

}
