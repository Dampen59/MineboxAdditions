package io.dampen59.mineboxadditions.utils;
import net.minecraft.network.chat.contents.TranslatableContents;

import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.utils.models.Location;
import io.dampen59.mineboxadditions.utils.models.Skill;
import io.dampen59.mineboxadditions.utils.models.SkillData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.*;

public class Utils {
    private static boolean isOnMinebox = false;

    private static LocalTime time = LocalTime.of(0, 0);
    private static boolean timeKnown = false;
    private static Location previousLocation = Location.UNKNOWN;
    private static Location location = Location.UNKNOWN;
    private static final Map<Skill, SkillData> skills = new EnumMap<>(Skill.class);

    public static boolean isOnMinebox() {
        return isOnMinebox;
    }

    public static boolean isInSpawn() {
        return location == Location.SPAWN;
    }

    public static boolean isInHome() {
        return location == Location.HOME;
    }

    public static boolean isInKokoko() {
        return location == Location.KOKOKO;
    }

    public static boolean isInQuadraPlains() {
        return location == Location.QUADRA_PLAINS;
    }

    public static boolean isInBambooPeak() {
        return location == Location.BAMBOO_PEAK;
    }

    public static boolean isInFrostbiteFortress() {
        return location == Location.FROSTBITE_FORTRESS;
    }

    public static boolean isInSandwhisperDunes() {
        return location == Location.SANDWHISPER_DUNES;
    }

    public static Location getPreviousLocation() { return previousLocation; }

    public static LocalTime getTime() { return time; }

    public static boolean isTimeKnown() { return timeKnown; }

    public static boolean tryUpdateTime(String timeStr) {
        try {
            time = LocalTime.parse(timeStr.trim(), java.time.format.DateTimeFormatter.ofPattern("H:mm"));
            timeKnown = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Location getLocation() {
        return location;
    }

    public static SkillData getSkill(Skill skill) {
        return skills.computeIfAbsent(skill, SkillData::new);
    }

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(Utils::onJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(Utils::onDisconnect);
    }

    private static void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        ServerData server = handler.getServerData();

        if (server != null) {
            String address = server.ip.toLowerCase();
            isOnMinebox = address.contains("minebox.co") || address.contains("minebox.fr");
        } else isOnMinebox = false;
    }

    private static void onDisconnect(ClientPacketListener handler, Minecraft client) {
        isOnMinebox = false;
        previousLocation = Location.UNKNOWN;
        location = Location.UNKNOWN;
    }

    public static void updateTime(String timeStr) {
        time = LocalTime.parse(timeStr);
    }

    public static void updateLocation(@Nullable Component footer) {
        if (!isOnMinebox) return;
        if (footer == null || footer.getSiblings().isEmpty()) return;

        String serverId = footer.getSiblings().getLast().getString().replaceAll("\\r?\\n", "");
        previousLocation = location;
        location = Location.from(serverId);
    }

    public static void showToastNotification(String prmTitle, String prmDescription) {

        Minecraft client = Minecraft.getInstance();
        if (client == null) return;

        client.execute(() -> client.getToastManager().addToast(
                new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                        Component.literal(prmTitle),
                        Component.literal(prmDescription)
                )
        ));
    }

    public static Component getPlayerServerName(String playerName) {
        Minecraft client = Minecraft.getInstance();

        if (client != null && client.player != null) {
            Collection<PlayerInfo> entries = client.player.connection.getOnlinePlayers();
            for (PlayerInfo entry : entries) {
                if (!entry.getProfile().name().equals(playerName)) continue;
                if (entry.getProfile().name() == null) break;
                return Component.literal(entry.getProfile().name());
            }
        }

        return Component.literal(playerName);
    }

    public static boolean itemHaveStats(ItemStack itemStack) {
        ItemLore loresList = itemStack.get(DataComponents.LORE);
        if (loresList == null) return false;

        for (Component lore : loresList.lines()) {
            for (Component sibling : lore.getSiblings()) {
                List<Component> nestedSiblings = sibling.getSiblings();
                for (Component nestedSibling : nestedSiblings) {
                    if (!(nestedSibling.getContents() instanceof TranslatableContents translatableContent)) continue;
                    String translationKey = translatableContent.getKey();
                    if (translationKey.contains("mbx.stats.")) return true;
                }
            }
        }
        return false;
    }

    public static boolean isMineboxItem(ItemStack itemStack) {
        CustomData itemData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return false;

        CompoundTag nbtData = itemData.copyTag();
        return nbtData != null && nbtData.contains("mbitems:id");
    }

    public static String getMineboxItemId(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return null;

        CustomData itemData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return null;

        CompoundTag nbtData = itemData.copyTag();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return null;

        return nbtData.getString("mbitems:id").orElse(null);
    }

    public static String processIdMismatch(String prmId) {
        switch (prmId) {
            case "transformed_material-bamboo":
                return "transformed_bamboo";
            case "bag_material-bamboo":
                return "bag_bamboo";
            case "crate_material-bamboo":
                return "crate_bamboo";
            case "barrel_material-bamboo":
                return "barrel_bamboo";
            case "enchanted_material-bamboo":
                return "enchanted_bamboo";

            case "transformed_material-carrot":
                return "transformed_carrot";
            case "bag_material-carrot":
                return "bag_carrot";
            case "crate_material-carrot":
                return "crate_carrot";
            case "barrel_material-carrot":
                return "barrel_carrot";
            case "enchanted_material-carrot":
                return "enchanted_carrot";

            case "transformed_material-beetroot":
                return "transformed_beetroot";
            case "bag_material-beetroot":
                return "bag_beetroot";
            case "crate_material-beetroot":
                return "crate_beetroot";
            case "barrel_material-beetroot":
                return "barrel_beetroot";
            case "enchanted_material-beetroot":
                return "enchanted_beetroot";

            case "transformed_material-cactus":
                return "transformed_cactus";
            case "bag_material-cactus":
                return "bag_cactus";
            case "crate_material-cactus":
                return "crate_cactus";
            case "barrel_material-cactus":
                return "barrel_cactus";
            case "enchanted_material-cactus":
                return "enchanted_cactus";

            case "transformed_material-cocoa_beans":
                return "transformed_cocoa_beans";
            case "bag_material-cocoa_beans":
                return "bag_cocoa_beans";
            case "crate_material-cocoa_beans":
                return "crate_cocoa_beans";
            case "barrel_material-cocoa_beans":
                return "barrel_cocoa_beans";
            case "enchanted_material-cocoa_beans":
                return "enchanted_cocoa_beans";

            case "transformed_material-kelp":
                return "transformed_kelp";
            case "bag_material-kelp":
                return "bag_kelp";
            case "crate_material-kelp":
                return "crate_kelp";
            case "barrel_material-kelp":
                return "barrel_kelp";
            case "enchanted_material-kelp":
                return "enchanted_kelp";

            case "transformed_material-melon_slice":
                return "transformed_melon_slice";
            case "bag_material-melon_slice":
                return "bag_melon_slice";
            case "crate_material-melon_slice":
                return "crate_melon_slice";
            case "barrel_material-melon_slice":
                return "barrel_melon_slice";
            case "enchanted_material-melon_slice":
                return "enchanted_melon_slice";

            case "transformed_material-nether_wart":
                return "transformed_nether_wart";
            case "bag_material-nether_wart":
                return "bag_nether_wart";
            case "crate_material-nether_wart":
                return "crate_nether_wart";
            case "barrel_material-nether_wart":
                return "barrel_nether_wart";
            case "enchanted_material-nether_wart":
                return "enchanted_nether_wart";

            case "transformed_material-potato":
                return "transformed_potato";
            case "bag_material-potato":
                return "bag_potato";
            case "crate_material-potato":
                return "crate_potato";
            case "barrel_material-potato":
                return "barrel_potato";
            case "enchanted_material-potato":
                return "enchanted_potato";

            case "transformed_material-pumpkin":
                return "transformed_pumpkin";
            case "bag_material-pumpkin":
                return "bag_pumpkin";
            case "crate_material-pumpkin":
                return "crate_pumpkin";
            case "barrel_material-pumpkin":
                return "barrel_pumpkin";
            case "enchanted_material-pumpkin":
                return "enchanted_pumpkin";

            case "transformed_material-sugar_cane":
                return "transformed_sugar_cane";
            case "bag_material-sugar_cane":
                return "bag_sugar_cane";
            case "crate_material-sugar_cane":
                return "crate_sugar_cane";
            case "barrel_material-sugar_cane":
                return "barrel_sugar_cane";
            case "enchanted_material-sugar_cane":
                return "enchanted_sugar_cane";

            case "transformed_material-sweet_berries":
                return "transformed_sweet_berries";
            case "bag_material-sweet_berries":
                return "bag_sweet_berries";
            case "crate_material-sweet_berries":
                return "crate_sweet_berries";
            case "barrel_material-sweet_berries":
                return "barrel_sweet_berries";
            case "enchanted_material-sweet_berries":
                return "enchanted_sweet_berries";

            case "transformed_material-wheat":
                return "transformed_wheat";
            case "bag_material-wheat":
                return "bag_wheat";
            case "crate_material-wheat":
                return "crate_wheat";
            case "barrel_material-wheat":
                return "barrel_wheat";
            case "enchanted_material-wheat":
                return "enchanted_wheat";

            default:
                return prmId;
        }
    }


    public static boolean isItemLooted(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return false;

        CustomData itemData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return false;

        CompoundTag nbtData = itemData.copyTag();
        if (nbtData == null || !nbtData.contains("mbitems:looted")) return false;

        return nbtData.getInt("mbitems:looted")
                .map(value -> value == 1)
                .orElse(false);
    }

    public static boolean isInventoryBaseItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getCustomName() == null) {
            return false;
        }

        Component customName = itemStack.getCustomName();

        if (customName.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            return "mbx.your_stats.title".equals(key) || "mbx.main_menu.title".equals(key);
        }

        return false;
    }


    public static int getItemSize(ItemStack itemStack) {
        if (itemStack == null) return 0;
        if (!isMineboxItem(itemStack)) return 0;

        ItemLore loresList = itemStack.get(DataComponents.LORE);
        if (loresList == null) return 0;

        for (Component lore : loresList.lines()) {
            ComponentContents content = lore.getContents();
            if (content instanceof TranslatableContents tl &&
                    "mbx.size".equals(tl.getKey())) {
                Object[] args = tl.getArgs();
                if (args.length > 0) {
                    return parseIntArg(args[0]);
                }
            }

            for (Component sibling : lore.getSiblings()) {
                ComponentContents sibContent = sibling.getContents();
                if (sibContent instanceof TranslatableContents tl2 &&
                        "mbx.size".equals(tl2.getKey())) {
                    Object[] args = tl2.getArgs();
                    if (args.length > 0) {
                        return parseIntArg(args[0]);
                    }
                }
            }
        }
        return 0;
    }

    public static String getMineboxItemUid(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return null;

        CustomData itemData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return null;

        CompoundTag nbtData = itemData.copyTag();
        if (nbtData == null) return null;

        CompoundTag persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
        if (persistentData == null || !persistentData.contains("mbitems:uid")) return null;

        return persistentData.getString("mbitems:uid").orElse(null);
    }

    public static MineboxItem findItemByName(List<MineboxItem> items, String itemName) {
        for (MineboxItem item : items) {
            if (itemName.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    public static void displayChatErrorMessage(String prmMessage) {
        Component message = Component.literal("❌ " + prmMessage)
                .setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(false));
        Minecraft.getInstance().player.sendSystemMessage(message);
    }

    public static void displayChatSuccessMessage(String prmMessage) {
        Component message = Component.literal("✔ " + prmMessage)
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(false));
        Minecraft.getInstance().player.sendSystemMessage(message);
    }

    public static void displayChatInfoMessage(String prmMessage) {
        Component message = Component.literal("\uD83D\uDEC8 " + prmMessage)
                .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withBold(false));
        Minecraft.getInstance().player.sendSystemMessage(message);
    }

    public boolean isHarvestableTextDisplay(Display.TextDisplay entity) {
        Component text = entity.getText();
        if (text.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            return key.contains("mbx.harvestable");
        }
        return false;
    }

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getModVersion() {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer("mineboxadditions").isPresent() ? FabricLoader.getInstance().getModContainer("mineboxadditions").get().getMetadata() : null;
        return modMetadata != null ? modMetadata.getVersion().getFriendlyString() : "unknown";
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int parseIntArg(Object arg) {
        if (arg == null) return 0;

        if (arg instanceof Component t) {
            String s = t.getString().trim();
            return tryParseInt(s);
        }

        if (arg instanceof Number n) {
            return n.intValue();
        }

        return tryParseInt(arg.toString().trim());
    }

    private static int tryParseInt(String s) {
        try {
            int space = s.indexOf(' ');
            if (space > 0) s = s.substring(0, space);
            int slash = s.indexOf('/');
            if (slash > 0) s = s.substring(0, slash);
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
