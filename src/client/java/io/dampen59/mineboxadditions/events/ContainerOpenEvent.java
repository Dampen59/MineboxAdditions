package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ExtraInventoryUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Arrays;

public class ContainerOpenEvent {
    private final State modState;

    private final String[] playerMenuTitles = new String[]{"Player menu", "Menu joueur", "Menu gracza"};

    private final String[] mermaidMenuTitles = new String[]{"Mermaid", "Sirène"};

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
                modState.getSocket().emit("C2SShopOfferEvent", translationKey);
            }
        } else {
            String containerTitleString = containerTitle.getString();

            if (Arrays.stream(playerMenuTitles).anyMatch(containerTitleString::contains)) {

                for (int i = 0; i < 4; i++) {

                    final int setId = i;
                    int yOffset = baseY * 30 + (55 * i); // 30
                    int textBoxX = baseX + 16 + 5; // 25
                    int textBoxY = baseY * 30 + 22 + 2 + (55 * i); // 30
                    TextFieldWidget setNameTextbox = new TextFieldWidget(client.textRenderer, textBoxX, textBoxY, 96, 22, Text.empty());

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
                            equipButtonRef[0].setMessage(Text.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"));
                        }
                        ExtraInventoryUtils.saveCurrentSetToSlotId(containerScreen.getScreenHandler().slots, setId);
                    }).dimensions(renameButtonX, renameButtonY, 16, 24).build();

                    ButtonWidget equipButton = ButtonWidget.builder(Text.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"), buttonWidget -> ExtraInventoryUtils.equipSet(containerScreen.getScreenHandler().slots, setId)).dimensions(equipButtonX, equipButtonY, 96, 22).build();

                    // Store the equip button reference for later update
                    equipButtonRef[0] = equipButton;

                    screen.addDrawableChild(setNameTextbox);
                    Screens.getButtons(containerScreen).add(renameSaveButton);
                    Screens.getButtons(containerScreen).add(equipButton);
                }
            } else if (Arrays.stream(mermaidMenuTitles).anyMatch(containerTitleString::contains)) {
                client.execute(() -> {
                    ItemStack mermaidRequest = containerScreen.getScreenHandler().getInventory().getStack(22);
                    String requestedItemTranslationKey = extractTranslationKey(mermaidRequest.getFormattedName());
                    int requestedItemQuantity = mermaidRequest.getCount();
                    this.modState.getSocket().emit("C2SMermaidRequest", requestedItemTranslationKey, requestedItemQuantity);
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
}
