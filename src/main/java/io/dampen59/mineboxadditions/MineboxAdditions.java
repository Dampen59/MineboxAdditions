package io.dampen59.mineboxadditions;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import io.dampen59.mineboxadditions.config.ConfigManager;
import io.dampen59.mineboxadditions.features.AutoIsland;
import io.dampen59.mineboxadditions.features.ShinyTracker;
import io.dampen59.mineboxadditions.features.SpellsThrottler;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoalDisplay;
import io.dampen59.mineboxadditions.features.shop.ShopManager;
import io.dampen59.mineboxadditions.features.item.ItemTooltip;
import io.dampen59.mineboxadditions.events.*;
import io.dampen59.mineboxadditions.features.hud.BossBarScanner;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.huds.ShopHud;
import io.dampen59.mineboxadditions.features.menu.MineboxMenuScreen;
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
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.stream.Collectors;

public class MineboxAdditions implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("mineboxadditions");
    public static final String NAMESPACE = "mineboxadditions";
    public static MineboxAdditions INSTANCE;

    public State state = null;
    public static KeyMapping openMenu;
    public static KeyMapping mountDismount;
    public static KeyMapping castSpellOne;
    public static KeyMapping castSpellTwo;
    public static KeyMapping castSpellThree;
    public static KeyMapping castSpellFour;

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
        SpellsThrottler.init();

        new SkyEvent();
        new BossBarScanner();
        new ServerEvents(state);
        new ContainerOpenEvent(state);
        new WorldRendererEvent();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));
        this.registerKeybinds();

        INSTANCE = this;
    }

    public void tick(Minecraft client) {
        Scheduler.INSTANCE.tick();

        if (openMenu.consumeClick()) {
            client.gui.setScreen(new MineboxMenuScreen());
        }

        if (mountDismount.consumeClick() && client.player != null) {
            Objects.requireNonNull(client.getConnection()).send(new ServerboundChatCommandPacket("ride"));
        }

        if (castSpellOne.consumeClick()) SpellsThrottler.tryCast(1, client);
        if (castSpellTwo.consumeClick()) SpellsThrottler.tryCast(2, client);
        if (castSpellThree.consumeClick()) SpellsThrottler.tryCast(3, client);
        if (castSpellFour.consumeClick()) SpellsThrottler.tryCast(4, client);
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
                    .then(ClientCommands.literal("test_shops")
                        .executes(context -> {
                            boolean frozen = ShopManager.toggleHudFreeze();
                            if (frozen) {
                                TextElement text = HudManager.INSTANCE.get(ShopHud.class)
                                        .getNamedElement("text", TextElement.class);
                                text.setLines(java.util.List.of(
                                        Component.literal("Shop 1: Item 1"),
                                        Component.literal("Shop 2: Item 2"),
                                        Component.literal("Shop 3: Item 3"),
                                        Component.literal("/mba debug test_shops to exit test mode")
                                ));
                                Utils.displayChatInfoMessage("Shop HUD is now in test mode. Rerun the command to exit.");
                            } else {
                                Utils.displayChatInfoMessage("Shop HUD test mode disabled.");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
        );
    }

    private static final KeyMapping.Category MBX_CATEGORY = new KeyMapping.Category(Identifier.withDefaultNamespace(NAMESPACE));

    private void registerKeybinds() {
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.menu.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                MBX_CATEGORY
        ));

        mountDismount = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.mount",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                MBX_CATEGORY
        ));

        castSpellOne = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.spellcast.one",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                MBX_CATEGORY
        ));

        castSpellTwo = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.spellcast.two",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                MBX_CATEGORY
        ));

        castSpellThree = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.spellcast.three",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                MBX_CATEGORY
        ));

        castSpellFour = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "mineboxadditions.strings.keybinds.spellcast.four",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                MBX_CATEGORY
        ));
    }
}
