package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.minebox.MineboxToast;
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

    public static boolean isStatsItem(ItemStack stack) {
        if (stack == null || stack.getCustomName() == null) {
            return false;
        }

        Text customName = stack.getCustomName();

        if (customName.getContent() instanceof TranslatableTextContent translatable) {
            return "mbx.your_stats.title".equals(translatable.getKey());
        }

        return false;
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
}
