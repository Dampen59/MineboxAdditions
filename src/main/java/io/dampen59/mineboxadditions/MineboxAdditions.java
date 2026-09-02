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
import io.dampen59.mineboxadditions.features.item.MuseumTracker;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        MuseumTracker.init();

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
                        sendDebugReport();
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

    private static final DateTimeFormatter DEBUG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void sendDebugReport() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SocketManager.ProtocolState protocolState = SocketManager.getState();
        boolean connected = SocketManager.getSocket().connected();
        boolean trusted = SocketManager.isTrusted();
        String socketId = connected ? SocketManager.getSocket().id() : null;

        List<Integer> rain = this.state.getWeatherState().getRainTimestamps();
        List<Integer> storm = this.state.getWeatherState().getStormTimestamps();
        var mermaid = ShopManager.getMermaid();
        String mermaidText = mermaid.itemTranslationKey != null
                ? String.format("%s x%d", mermaid.itemTranslationKey, mermaid.quantity)
                : "none";

        sendLine(header("MineboxAdditions Debug Report"));
        sendLine(kv("Mod Version", Component.literal(Utils.getModVersion()).withStyle(ChatFormatting.WHITE)));
        sendLine(kv("Protocol State", stateComponent(protocolState, trusted)));
        sendLine(kv("Socket", connected
                ? Component.literal("connected (" + socketId + ")").withStyle(ChatFormatting.GREEN)
                : Component.literal("disconnected").withStyle(ChatFormatting.RED)));
        sendLine(kv("Weather", Component.literal(rain.size() + " rain, " + storm.size() + " storm timestamp(s)").withStyle(ChatFormatting.WHITE)));
        sendLine(kv("Shiny Tracked", Component.literal(String.valueOf(ShinyTracker.getShinyCount())).withStyle(ChatFormatting.WHITE)));
        sendLine(kv("Mermaid", Component.literal(mermaidText).withStyle(ChatFormatting.WHITE)));
        sendLine(kv("Museum Missing", Component.literal(String.valueOf(this.state.getMissingMuseumItemIds().size())).withStyle(ChatFormatting.WHITE)));

        String clipboardReport = buildClipboardReport(protocolState, trusted, connected, socketId, rain, storm, mermaidText);
        Component copyLine = Component.literal("[📋 Click to copy full report]")
                .withStyle(s -> s
                        .withColor(ChatFormatting.AQUA)
                        .withBold(true)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.CopyToClipboard(clipboardReport))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Copy the full debug report to your clipboard")
                                .withStyle(ChatFormatting.GRAY))));
        sendLine(copyLine);
    }

    private void sendLine(Component component) {
        Minecraft.getInstance().player.sendSystemMessage(component);
    }

    private static Component header(String title) {
        return Component.literal("═══ " + title + " ═══").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true));
    }

    private static Component kv(String label, Component value) {
        return Component.literal(" " + label + ": ").withStyle(ChatFormatting.GRAY).append(value);
    }

    private static Component stateComponent(SocketManager.ProtocolState state, boolean trusted) {
        ChatFormatting color = switch (state) {
            case READY_TRUSTED -> ChatFormatting.GREEN;
            case READY_UNTRUSTED -> ChatFormatting.YELLOW;
            case CONNECTED, NEGOTIATING, AUTHENTICATING -> ChatFormatting.AQUA;
        };
        String label = state.name() + (state == SocketManager.ProtocolState.READY_TRUSTED || state == SocketManager.ProtocolState.READY_UNTRUSTED
                ? "" : "…");
        return Component.literal(label).withStyle(color);
    }

    private String buildClipboardReport(
            SocketManager.ProtocolState protocolState,
            boolean trusted,
            boolean connected,
            String socketId,
            List<Integer> rain,
            List<Integer> storm,
            String mermaidText
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MineboxAdditions Debug Report ===\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DEBUG_TIMESTAMP_FORMAT)).append("\n");
        sb.append("Mod Version: ").append(Utils.getModVersion()).append("\n");
        sb.append("Protocol State: ").append(protocolState.name()).append(" (trusted: ").append(trusted).append(")\n");
        sb.append("Socket: ").append(connected ? "connected (" + socketId + ")" : "disconnected").append("\n");
        sb.append("Rain Timestamps (").append(rain.size()).append("): ")
                .append(rain.stream().map(String::valueOf).collect(Collectors.joining(", "))).append("\n");
        sb.append("Storm Timestamps (").append(storm.size()).append("): ")
                .append(storm.stream().map(String::valueOf).collect(Collectors.joining(", "))).append("\n");
        sb.append("Shiny Tracked: ").append(ShinyTracker.getShinyCount()).append("\n");
        sb.append("Mermaid: ").append(mermaidText).append("\n");
        sb.append("Museum Missing: ").append(this.state.getMissingMuseumItemIds().size()).append("\n");
        sb.append("Items Loaded: ").append(this.state.getMbxItems() != null ? this.state.getMbxItems().size() : "not loaded").append("\n");
        sb.append("Bestiary Loaded: ").append(this.state.getMbxBestiary() != null ? this.state.getMbxBestiary().size() : "not loaded").append("\n");
        sb.append("Insects Loaded: ").append(this.state.getInsects() != null ? this.state.getInsects().size() : "not loaded").append("\n");
        return sb.toString();
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
