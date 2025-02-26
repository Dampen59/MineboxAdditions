package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.minebox.MineboxItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.List;

public class Utils {

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
        playSound(SoundEvents.ENTITY_PLAYER_LEVELUP);

        client.player.sendMessage(message, false);
    }
}
