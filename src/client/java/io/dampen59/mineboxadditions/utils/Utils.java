package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.minebox.MineboxChatFlag;
import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.minebox.MineboxToast;
import io.dampen59.mineboxadditions.minebox.ParsedMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

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
                    if (!(nestedSibling.getContent() instanceof TranslatableTextContent)) continue;
                    TranslatableTextContent translatableContent = (TranslatableTextContent) nestedSibling.getContent();
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
        if (nbtData == null || !nbtData.contains("mbitems:id")) return false;

        return true;
    }

    public static String getMineboxItemId(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return null;

        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return null;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return null;

        return nbtData.getString("mbitems:id");
    }

    public static String getMineboxItemUid(ItemStack itemStack) {
        if (!isMineboxItem(itemStack)) return null;

        NbtComponent itemData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return null;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null) return null;

        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent");
        if (persistentData == null || !persistentData.contains("mbitems:uid")) return null;

        return persistentData.getString("mbitems:uid");
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

        Text mobText = Text.literal( "[" + prmMobName + "]")
                .setStyle(Style.EMPTY.withColor(0xFEFE00).withBold(true));

        Text endMessage = Text.literal(" ! Click on this message to send a teleport request.")
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa " + prmPlayerName)));


        Text message = baseMessage.copy().append(playerText).append(baseMessageNext).append(mobText).append(endMessage);

        client.player.sendMessage(message, false);

        playSound(SoundEvents.ENTITY_PLAYER_LEVELUP);
    }

    public static String actionBarDataToChatLang(String prmActionBarData) {

        String prefix = "GLOBAL";
        String suffix = "";

        int startIndex = prmActionBarData.indexOf(prefix);
        int endIndex = prmActionBarData.indexOf(suffix, startIndex + prefix.length());

        String extracted = null;
        if (startIndex != -1 && endIndex != -1) {
            extracted = prmActionBarData.substring(startIndex + prefix.length(), endIndex);
        }

        return switch (extracted) {
            case "끰" -> "fr";
            case "끮" -> "en";
            case "끯" -> "es";
            case "낃" -> "ru";
            case "낁" -> "pt";
            case "끬" -> "de";
            case "낊" -> "cn";
            case "낀" -> "pl";
            case "끺" -> "it";
            case "끻" -> "jp";
            case "끾" -> "nl";
            case "낈" -> "tr";
            default -> "en";
        };

    }

    public static String getChatFlagByLang(List<MineboxChatFlag> mbxChatFlags, String lang) {
        for (MineboxChatFlag item : mbxChatFlags) {
            if (item.getLang().equalsIgnoreCase(lang)) {
                return item.getFlag();
            }
        }
        return mbxChatFlags.getFirst().getFlag();
    }


    public static ParsedMessage extractPlayerNameAndMessage(String input) {
        if (input == null || !input.contains(": ")) return null;
        int lastSpecialCharIndex = input.lastIndexOf('', input.indexOf(": "));
        if (lastSpecialCharIndex == -1) return null;
        String playerName = input.substring(lastSpecialCharIndex + 1, input.indexOf(": ")).trim();
        String message = input.substring(input.indexOf(": ") + 2).trim();
        return new ParsedMessage(playerName, message);
    }

    public static void displayChatMessage(String prmFlag, String prmPlayerName, String prmMessageContent) {

        MinecraftClient client = MinecraftClient.getInstance();

        Text baseMessage = Text.literal(prmFlag);

        Text playerName = Text.literal(" " + prmPlayerName)
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(false));

        Text playerMessage = Text.literal(": " + prmMessageContent)
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false));

        Text message = baseMessage.copy().append(playerName).append(playerMessage);

        client.player.sendMessage(message, false);
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

}
