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
import io.dampen59.mineboxadditions.network.SocketManager;
import io.dampen59.mineboxadditions.state.AudioDeviceState;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.AudioUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MineboxAdditionsClient implements ClientModInitializer {

    public final State modState = new State();
    public static MineboxAdditionsClient INSTANCE;

    private static KeyBinding keyBinding;
    public static KeyBinding openEditMode;

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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new AudioDeviceScreen());
            }
            if (openEditMode.wasPressed()) {
                client.setScreen(new HudEditorScreen(AutoConfig.getConfigHolder(ModConfig.class).getConfig()));
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

}
