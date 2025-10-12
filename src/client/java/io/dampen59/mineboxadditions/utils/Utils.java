package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.features.item.MineboxItem;
import io.dampen59.mineboxadditions.features.hud.MineboxToast;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class Utils {

    public static void showShopToastNotification(String prmShopName, String prmTitle, String prmDescription) {

        String texturePath = shopNameToTexture(prmShopName);
        if (texturePath == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.getToastManager().add(new MineboxToast(
                client.textRenderer,
                Identifier.of("mineboxadditions", texturePath),
                Text.of(prmTitle),
                Text.of(prmDescription)
        ));
    }

    public static void showToastNotification(String prmTitle, String prmDescription) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.getToastManager().add(
                new SystemToast(
                        SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal(prmTitle),
                        Text.literal(prmDescription)
                )
        );
    }

    public static void playSound(SoundEvent prmSound) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        client.player.playSound(
                prmSound,
                1.0f,
                1.0f
        );

    }

    public static boolean itemHaveStats(ItemStack itemStack) {
        LoreComponent loresList = itemStack.get(DataComponentTypes.LORE);
        if (loresList == null) return false;

        for (Text lore : loresList.lines()) {
            for (Text sibling : lore.getSiblings()) {
                List<Text> nestedSiblings = sibling.getSiblings();
                for (Text nestedSibling : nestedSiblings) {
                    if (!(nestedSibling.getContent() instanceof TranslatableTextContent translatableContent)) continue;
                    String translationKey = translatableContent.getKey();
                    if (translationKey.contains("mbx.stats.")) return true;
                }
            }
        }
        return false;
    }

    public static boolean isMineboxItem(ItemStack itemStack) {
        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return false;

        NbtCompound nbtData = itemData.copyNbt();
        return nbtData != null && nbtData.contains("mbitems:id");
    }

    public static String getMineboxItemId(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return null;

        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return null;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return null;

        return nbtData.getString("mbitems:id").orElse(null);
    }

    public static String processIdMismatch(String prmId) {
        switch (prmId) {
            case "transformed_material-bamboo":
                return "transformed_bamboo";
            case "bag_material-bamboo":
                return "bag_bamboo";
            case "crate_bamboo":
                return "crate_material-bamboo";
            case "barrel_bamboo":
                return "barrel_material-bamboo";
            case "enchanted_bamboo":
                return "enchanted_material-bamboo";

            case "transformed_material-carrot":
                return "transformed_carrot";
            case "bag_material-carrot":
                return "bag_carrot";
            case "crate_carrot":
                return "crate_material-carrot";
            case "barrel_carrot":
                return "barrel_material-carrot";
            case "enchanted_carrot":
                return "enchanted_material-carrot";

            case "transformed_material-beetroot":
                return "transformed_beetroot";
            case "bag_material-beetroot":
                return "bag_beetroot";
            case "crate_beetroot":
                return "crate_material-beetroot";
            case "barrel_beetroot":
                return "barrel_material-beetroot";
            case "enchanted_beetroot":
                return "enchanted_material-beetroot";

            case "transformed_material-cactus":
                return "transformed_cactus";
            case "bag_material-cactus":
                return "bag_cactus";
            case "crate_cactus":
                return "crate_material-cactus";
            case "barrel_cactus":
                return "barrel_material-cactus";
            case "enchanted_cactus":
                return "enchanted_material-cactus";

            case "transformed_material-cocoa_beans":
                return "transformed_cocoa_beans";
            case "bag_material-cocoa_beans":
                return "bag_cocoa_beans";
            case "crate_cocoa_beans":
                return "crate_material-cocoa_beans";
            case "barrel_cocoa_beans":
                return "barrel_material-cocoa_beans";
            case "enchanted_cocoa_beans":
                return "enchanted_material-cocoa_beans";

            case "transformed_material-kelp":
                return "transformed_kelp";
            case "bag_material-kelp":
                return "bag_kelp";
            case "crate_kelp":
                return "crate_material-kelp";
            case "barrel_kelp":
                return "barrel_material-kelp";
            case "enchanted_kelp":
                return "enchanted_material-kelp";

            case "transformed_material-melon_slice":
                return "transformed_melon_slice";
            case "bag_material-melon_slice":
                return "bag_melon_slice";
            case "crate_melon_slice":
                return "crate_material-melon_slice";
            case "barrel_melon_slice":
                return "barrel_material-melon_slice";
            case "enchanted_melon_slice":
                return "enchanted_material-melon_slice";

            case "transformed_material-nether_wart":
                return "transformed_nether_wart";
            case "bag_material-nether_wart":
                return "bag_nether_wart";
            case "crate_nether_wart":
                return "crate_material-nether_wart";
            case "barrel_nether_wart":
                return "barrel_material-nether_wart";
            case "enchanted_nether_wart":
                return "enchanted_material-nether_wart";

            case "transformed_material-potato":
                return "transformed_potato";
            case "bag_material-potato":
                return "bag_potato";
            case "crate_potato":
                return "crate_material-potato";
            case "barrel_potato":
                return "barrel_material-potato";
            case "enchanted_potato":
                return "enchanted_material-potato";

            case "transformed_material-pumpkin":
                return "transformed_pumpkin";
            case "bag_material-pumpkin":
                return "bag_pumpkin";
            case "crate_pumpkin":
                return "crate_material-pumpkin";
            case "barrel_pumpkin":
                return "barrel_material-pumpkin";
            case "enchanted_pumpkin":
                return "enchanted_material-pumpkin";

            case "transformed_material-sugar_cane":
                return "transformed_sugar_cane";
            case "bag_material-sugar_cane":
                return "bag_sugar_cane";
            case "crate_sugar_cane":
                return "crate_material-sugar_cane";
            case "barrel_sugar_cane":
                return "barrel_material-sugar_cane";
            case "enchanted_sugar_cane":
                return "enchanted_material-sugar_cane";

            case "transformed_material-sweet_berries":
                return "transformed_sweet_berries";
            case "bag_material-sweet_berries":
                return "bag_sweet_berries";
            case "crate_sweet_berries":
                return "crate_material-sweet_berries";
            case "barrel_sweet_berries":
                return "barrel_material-sweet_berries";
            case "enchanted_sweet_berries":
                return "enchanted_material-sweet_berries";

            case "transformed_material-wheat":
                return "transformed_wheat";
            case "bag_material-wheat":
                return "bag_wheat";
            case "crate_wheat":
                return "crate_material-wheat";
            case "barrel_wheat":
                return "barrel_material-wheat";
            case "enchanted_wheat":
                return "enchanted_material-wheat";
            default:
                return prmId;
        }
    }


    public static boolean isItemLooted(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return false;

        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return false;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:looted")) return false;

        return nbtData.getInt("mbitems:looted")
                .map(value -> value == 1)
                .orElse(false);
    }

    public static boolean isInventoryBaseItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getCustomName() == null) {
            return false;
        }

        Text customName = itemStack.getCustomName();

        if (customName.getContent() instanceof TranslatableTextContent translatable) {
            String key = translatable.getKey();
            return "mbx.your_stats.title".equals(key) || "mbx.main_menu.title".equals(key);
        }

        return false;
    }


    public static int getItemSize(ItemStack itemStack) {
        if (itemStack == null) return 0;
        if (!isMineboxItem(itemStack)) return 0;

        LoreComponent loresList = itemStack.get(DataComponentTypes.LORE);
        if (loresList == null) return 0;

        for (Text lore : loresList.lines()) {
            TextContent content = lore.getContent();
            if (content instanceof TranslatableTextContent tl &&
                    "mbx.size".equals(tl.getKey())) {
                Object[] args = tl.getArgs();
                if (args.length > 0) {
                    return parseIntArg(args[0]);
                }
            }

            for (Text sibling : lore.getSiblings()) {
                TextContent sibContent = sibling.getContent();
                if (sibContent instanceof TranslatableTextContent tl2 &&
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

        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return null;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null) return null;

        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
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

    public static void shinyFoundAlert(String prmPlayerName, String prmMobName) {

        MinecraftClient client = MinecraftClient.getInstance();

        Text baseMessage = Text.literal("The player ")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false));

        Text playerText = Text.literal(prmPlayerName)
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true));

        Text baseMessageNext = Text.literal(" found a shiny ")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false));

        Text mobText = Text.literal("[" + prmMobName + "]")
                .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));

        Text endMessage = Text.literal(" ! Click on this message to send a teleport request.")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false)
                        .withClickEvent(new ClickEvent.RunCommand("/tpa " + prmPlayerName)));


        Text message = baseMessage.copy().append(playerText).append(baseMessageNext).append(mobText).append(endMessage);

        client.player.sendMessage(message, false);

        playSound(SoundEvents.ENTITY_PLAYER_LEVELUP);
    }

    public static String shopNameToTexture(String prmShopName) {
        String sanitizedName = prmShopName.toLowerCase().trim();
        String returnVal = "textures/toasts/shops/";

        switch (sanitizedName) {
            case "bakery" -> returnVal += "bakery.png";
            case "mouse" -> returnVal += "mouse.png";
            case "cocktail" -> returnVal += "cocktail.png";
            case "buckstar" -> returnVal += "buckstar.png";
            default -> returnVal = null;
        }

        return returnVal;
    }

    public static void displayChatErrorMessage(String prmMessage) {
        Text message = Text.literal("❌ " + prmMessage)
                .setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(false));
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }

    public static void displayChatSuccessMessage(String prmMessage) {
        Text message = Text.literal("✔ " + prmMessage)
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false));
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }

    public static void displayChatInfoMessage(String prmMessage) {
        Text message = Text.literal("\uD83D\uDEC8 " + prmMessage)
                .setStyle(Style.EMPTY.withColor(Formatting.BLUE).withBold(false));
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }

    public boolean isHarvestableTextDisplay(DisplayEntity.TextDisplayEntity entity) {
        Text text = entity.getText();
        if (text.getContent() instanceof TranslatableTextContent translatable) {
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

        if (arg instanceof Text t) {
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
