package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ExtraInventoryUtils;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContainerOpenEvent {
    private final State modState;

    private final String[] playerMenuTitles = new String[] { "Player menu", "Menu joueur", "Menu gracza" };

    private final String[] mermaidMenuTitles = new String[] { "Mermaid", "Sirène" };

    private final String[] jobsMenuTitles = new String[] { "Jobs", "Métiers" };

    public ContainerOpenEvent(State modState) {
        this.modState = modState;
        ScreenEvents.AFTER_INIT.register(this::onContainerOpened);
    }

    private void onContainerOpened(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof GenericContainerScreen containerScreen)) {
            return;
        }

        int slotsCount = containerScreen.getScreenHandler().slots.size();
        Text containerTitle = containerScreen.getTitle();
        String translationKey = extractTranslationKey(containerTitle);

        int baseX = 5;
        int baseY = scaledHeight / 100;

        if (translationKey != null) {
            if (slotsCount <= 45) {
                SocketManager.getSocket().emit("C2SShopOfferEvent", translationKey);
            }
        } else {
            String containerTitleString = containerTitle.getString();

            if (Arrays.stream(playerMenuTitles).anyMatch(containerTitleString::contains)) {

                for (int i = 0; i < 4; i++) {

                    final int setId = i;
                    int yOffset = baseY * 30 + (55 * i); // 30
                    int textBoxX = baseX + 16 + 5; // 25
                    int textBoxY = baseY * 30 + 22 + 2 + (55 * i); // 30
                    TextFieldWidget setNameTextbox = new TextFieldWidget(client.textRenderer, textBoxX, textBoxY, 96,
                            22, Text.empty());

                    int renameButtonX = baseX; // 25
                    int renameButtonY = baseY * 30 + 12 + (55 * i); // 30
                    int equipButtonX = textBoxX;
                    int equipButtonY = baseY * 30 + (55 * i); // 30

                    final ButtonWidget[] equipButtonRef = new ButtonWidget[1];

                    ButtonWidget renameSaveButton = ButtonWidget.builder(Text.literal("\uD83D\uDCBE"), buttonWidget -> {
                        String newName = setNameTextbox.getText();
                        if (!newName.isEmpty()) {
                            ExtraInventoryUtils.setSetName(setId, newName);
                            // Update the equip button label
                            equipButtonRef[0]
                                    .setMessage(Text.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"));
                        }
                        ExtraInventoryUtils.saveCurrentSetToSlotId(containerScreen.getScreenHandler().slots, setId);
                    }).dimensions(renameButtonX, renameButtonY, 16, 24).build();

                    ButtonWidget equipButton = ButtonWidget
                            .builder(Text.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"),
                                    buttonWidget -> ExtraInventoryUtils
                                            .equipSet(containerScreen.getScreenHandler().slots, setId))
                            .dimensions(equipButtonX, equipButtonY, 96, 22).build();

                    // Store the equip button reference for later update
                    equipButtonRef[0] = equipButton;

                    screen.addDrawableChild(setNameTextbox);
                    Screens.getButtons(containerScreen).add(renameSaveButton);
                    Screens.getButtons(containerScreen).add(equipButton);
                }
            } else if (Arrays.stream(mermaidMenuTitles).anyMatch(containerTitleString::contains)) {
                client.execute(() -> {
                    ItemStack mermaidRequest = containerScreen.getScreenHandler().getInventory().getStack(22);

                    if (!Utils.isMineboxItem(mermaidRequest))
                        return;

                    String itemId = Utils.getMineboxItemId(mermaidRequest);
                    int requestedItemQuantity = mermaidRequest.getCount();

                    Text nameText = mermaidRequest.getFormattedName();
                    String requestedItemTranslationKey = extractTranslationKey(nameText);

                    String targetResourceKey = null;

                    // Ressources transfos, sacs, etc
                    if (requestedItemTranslationKey.startsWith("mbx.items.container.")) {
                        TranslatableTextContent targetResource = findDeepestTranslatableContent(nameText);
                        targetResourceKey = targetResource.getKey();
                    }

                    SocketManager.getSocket().emit("C2SMermaidRequest", itemId, requestedItemQuantity,
                            requestedItemTranslationKey, targetResourceKey);

                });
            } else if (Arrays.stream(jobsMenuTitles).anyMatch(containerTitleString::contains)) {
                final ScreenHandler handler = containerScreen.getScreenHandler();
                final int delayTicks = 10;

                class JobGuiSlotChecker {
                    int ticks = 0;
                    boolean done = false;
                }

                JobGuiSlotChecker checker = new JobGuiSlotChecker();

                // Jobs slot IDs
                final int[] targetSlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24 };
                List<String> jobLines = new ArrayList<>();

                ClientTickEvents.END_CLIENT_TICK.register(mc -> {
                    if (checker.done)
                        return;

                    checker.ticks++;
                    if (checker.ticks >= delayTicks) {
                        int maxJobNameLength = 0;
                        for (int slotIndex : targetSlots) {
                            if (slotIndex < handler.slots.size()) {
                                ItemStack stack = handler.slots.get(slotIndex).getStack();
                                if (!stack.isEmpty()) {
                                    String jobName = stack.getName().getString();
                                    LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
                                    if (loreComponent == null)
                                        return;

                                    Integer level = null;
                                    Integer xp = null;
                                    Integer xpMax = null;

                                    for (Text lore : loreComponent.lines()) {
                                        String plain = lore.getString().replaceAll("[^\\d/]", "");

                                        if ((lore.getString().contains("Level") || lore.getString().contains("Niveau"))
                                                && plain.contains("/")) {
                                            String[] parts = plain.split("/");
                                            if (parts.length == 2) {
                                                try {
                                                    level = Integer.parseInt(parts[0]);
                                                } catch (NumberFormatException ignored) {
                                                }
                                            }
                                        }

                                        if (lore.getString().contains("􀁐") && plain.contains("/")) {
                                            String[] parts = plain.split("/");
                                            if (parts.length == 2) {
                                                try {
                                                    xp = Integer.parseInt(parts[0]);
                                                    xpMax = Integer.parseInt(parts[1]);
                                                } catch (NumberFormatException ignored) {
                                                }
                                            }
                                        }
                                    }

                                    if (level != null && xp != null && xpMax != null) {
                                        maxJobNameLength = Math.max(maxJobNameLength, jobName.length());
                                        jobLines.add(jobName + "|" + level + "|" + xp + "|" + xpMax);
                                    }
                                }
                            }
                        }
                        checker.done = true;

                        StringBuilder jobData2Clipboard = new StringBuilder();
                        for (String line : jobLines) {
                            String[] parts = line.split("\\|");
                            String jobName = parts[0];
                            int level = Integer.parseInt(parts[1]);
                            int xp = Integer.parseInt(parts[2]);
                            int xpMax = Integer.parseInt(parts[3]);

                            jobData2Clipboard.append(String.format("%-" + (maxJobNameLength + 2) + "s %3d (%d/%d)%n",
                                    jobName + ":", level, xp, xpMax));
                        }

                        String clipboardText = jobData2Clipboard.toString().trim();
                        MinecraftClient.getInstance().keyboard.setClipboard(clipboardText);
                    }
                });
            }

        }
    }

    private String extractTranslationKey(Text text) {
        if (text.getContent() instanceof TranslatableTextContent translatable) {
            return translatable.getKey();
        }
        for (Text sibling : text.getSiblings()) {
            String key = extractTranslationKey(sibling);
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    private TranslatableTextContent findDeepestTranslatableContent(Text text) {
        TextContent content = text.getContent();
        if (content instanceof TranslatableTextContent translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Text innerText) {
                    TranslatableTextContent nested = findDeepestTranslatableContent(innerText);
                    if (nested != null)
                        return nested;
                }
            }
            return translatable;
        }

        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent siblingResult = findDeepestTranslatableContent(sibling);
            if (siblingResult != null)
                return siblingResult;
        }

        return null;
    }
}
