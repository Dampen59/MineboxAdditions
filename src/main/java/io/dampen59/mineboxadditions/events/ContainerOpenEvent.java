package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ExtraInventoryUtils;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private void onContainerOpened(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof ContainerScreen containerScreen)) {
            return;
        }

        int slotsCount = containerScreen.getMenu().slots.size();
        Component containerTitle = containerScreen.getTitle();
        String translationKey = extractTranslationKey(containerTitle);

        int baseX = 5;
        int baseY = scaledHeight / 100;

        if (translationKey != null) {
            if (slotsCount <= 45) {
                try {
                    SocketManager.getSocket().emit("C2SShopOffer", new JSONObject().put("itemName", translationKey));
                } catch (org.json.JSONException e) {
                    System.out.println("Failed to serialize shop offer: " + e.getMessage());
                }
            }
        } else {
            String containerTitleString = containerTitle.getString();

            if (Arrays.stream(playerMenuTitles).anyMatch(containerTitleString::contains)) {

                for (int i = 0; i < 4; i++) {

                    final int setId = i;
                    int yOffset = baseY * 30 + (55 * i); // 30
                    int textBoxX = baseX + 16 + 5; // 25
                    int textBoxY = baseY * 30 + 22 + 2 + (55 * i); // 30
                    EditBox setNameTextbox = new EditBox(client.font, textBoxX, textBoxY, 96,
                            22, Component.empty());

                    int renameButtonX = baseX; // 25
                    int renameButtonY = baseY * 30 + 12 + (55 * i); // 30
                    int equipButtonX = textBoxX;
                    int equipButtonY = baseY * 30 + (55 * i); // 30

                    final Button[] equipButtonRef = new Button[1];

                    Button renameSaveButton = Button.builder(Component.literal("\uD83D\uDCBE"), buttonWidget -> {
                        String newName = setNameTextbox.getValue();
                        if (!newName.isEmpty()) {
                            ExtraInventoryUtils.setSetName(setId, newName);
                            // Update the equip button label
                            equipButtonRef[0]
                                    .setMessage(Component.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"));
                        }
                        ExtraInventoryUtils.saveCurrentSetToSlotId(containerScreen.getMenu().slots, setId);
                    }).bounds(renameButtonX, renameButtonY, 16, 24).build();

                    Button equipButton = Button
                            .builder(Component.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"),
                                    buttonWidget -> ExtraInventoryUtils
                                            .equipSet(containerScreen.getMenu().slots, setId))
                            .bounds(equipButtonX, equipButtonY, 96, 22).build();

                    // Store the equip button reference for later update
                    equipButtonRef[0] = equipButton;

                    screen.addRenderableWidget(setNameTextbox);
                    containerScreen.addRenderableWidget(renameSaveButton);
                    containerScreen.addRenderableWidget(equipButton);
                }
            } else if (Arrays.stream(mermaidMenuTitles).anyMatch(containerTitleString::contains)) {
                if (!(containerScreen.getMenu() instanceof ChestMenu chestMenu)) return;

                final int[] ticks = {0};
                final boolean[] done = {false};

                ClientTickEvents.END_CLIENT_TICK.register(mc -> {
                    if (done[0]) return;
                    if (++ticks[0] < 5) return;
                    done[0] = true;

                    ItemStack mermaidRequest = chestMenu.getContainer().getItem(22);
                    if (!Utils.isMineboxItem(mermaidRequest)) return;

                    String itemId = Utils.getMineboxItemId(mermaidRequest);
                    int requestedItemQuantity = mermaidRequest.getCount();
                    Component nameText = mermaidRequest.getHoverName();
                    String requestedItemTranslationKey = extractTranslationKey(nameText);
                    if (requestedItemTranslationKey == null) return;

                    String targetResourceKey = null;
                    if (requestedItemTranslationKey.startsWith("mbx.items.container.")) {
                        TranslatableContents targetResource = findDeepestTranslatableContent(nameText);
                        if (targetResource != null) targetResourceKey = targetResource.getKey();
                    }

                    try {
                        JSONObject translation = new JSONObject()
                                .put("key", requestedItemTranslationKey)
                                .put("args", targetResourceKey != null ? new JSONArray(List.of(targetResourceKey)) : new JSONArray());
                        JSONObject mermaidRequestPayload = new JSONObject()
                                .put("itemId", itemId)
                                .put("quantity", requestedItemQuantity)
                                .put("translation", translation);
                        SocketManager.getSocket().emit("C2SMermaidRequest", mermaidRequestPayload);
                    } catch (org.json.JSONException e) {
                        System.out.println("Failed to serialize mermaid request: " + e.getMessage());
                    }
                });
            } else if (Arrays.stream(jobsMenuTitles).anyMatch(containerTitleString::contains)) {
                final AbstractContainerMenu handler = containerScreen.getMenu();
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
                                ItemStack stack = handler.slots.get(slotIndex).getItem();
                                if (!stack.isEmpty()) {
                                    String jobName = stack.getHoverName().getString();
                                    ItemLore loreComponent = stack.get(DataComponents.LORE);
                                    if (loreComponent == null)
                                        return;

                                    Integer level = null;
                                    Integer xp = null;
                                    Integer xpMax = null;

                                    for (Component lore : loreComponent.lines()) {
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
                        Minecraft.getInstance().keyboardHandler.setClipboard(clipboardText);
                    }
                });
            }

        }
    }

    private String extractTranslationKey(Component text) {
        if (text.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        for (Component sibling : text.getSiblings()) {
            String key = extractTranslationKey(sibling);
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    private TranslatableContents findDeepestTranslatableContent(Component text) {
        ComponentContents content = text.getContents();
        if (content instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component innerText) {
                    TranslatableContents nested = findDeepestTranslatableContent(innerText);
                    if (nested != null)
                        return nested;
                }
            }
            return translatable;
        }

        for (Component sibling : text.getSiblings()) {
            TranslatableContents siblingResult = findDeepestTranslatableContent(sibling);
            if (siblingResult != null)
                return siblingResult;
        }

        return null;
    }
}
