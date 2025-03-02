package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ExtraInventoryUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContainerOpenEvent {
    private final State modState;
    private final Map<String, List<String>> shopOffers = Map.of(
            "Mouse", List.of("mbx.items.emmental_cheese.name", "mbx.items.cheddar_cheese.name"),
            "Buckstar", List.of("mbx.items.coffee_gray.name", "mbx.items.coffee_green.name", "mbx.items.coffee_white.name", "mbx.items.coffee_yellow.name"),
            "Bakery", List.of("mbx.items.flour.name", "mbx.items.baking_powder.name", "mbx.items.baguette.name", "mbx.items.croissant.name", "mbx.items.blue_macaron.name", "mbx.items.green_macaron.name", "mbx.items.orange_macaron.name", "mbx.items.yellow_macaron.name"),
            "Cocktail", List.of("mbx.items.blue_cocktail.name", "mbx.items.orange_cocktail.name", "mbx.items.red_cocktail.name", "mbx.items.yellow_cocktail.name", "mbx.items.lemon.name", "mbx.items.avocado.name", "mbx.items.pineapple.name")
    );

    private final String[] playerMenuTitles = new String[]{"Player menu", "Menu joueur", "Menu gracza"};

    public ContainerOpenEvent(State modState) {
        this.modState = modState;
        ScreenEvents.AFTER_INIT.register(this::onContainerOpened);
    }

    private void onContainerOpened(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof GenericContainerScreen containerScreen)) {
            return;
        }

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        int slotsCount = containerScreen.getScreenHandler().slots.size();
        Text containerTitle = containerScreen.getTitle();
        String translationKey = extractTranslationKey(containerTitle);

        int baseX = 5;
        int baseY = scaledHeight / 100;

        if (translationKey != null) {
            if (slotsCount <= 45 && config.networkFeatures.sendShopsAlerts) {
                shopOffers.forEach((shop, keys) -> {
                    if (keys.contains(translationKey)) {
                        modState.getSocket().emit("C2SShopOfferEvent", shop, translationKey);
                    }
                });
            }
        } else {
            String containerTitleString = containerTitle.getString();
            if (Arrays.stream(playerMenuTitles).anyMatch(containerTitleString::contains)) {

                for (int i = 0; i < 4; i++) {

                    final int setId = i;
                    int yOffset = baseY * 30 + (55 * i); // 30
                    int textBoxX = baseX * 1 + 16 + 5; // 25
                    int textBoxY = baseY * 30 + 22 + 2 + (55 * i); // 30
                    TextFieldWidget setNameTextbox = new TextFieldWidget(client.textRenderer, textBoxX, textBoxY, 96, 22, Text.empty());

                    int renameButtonX = baseX * 1; // 25
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

                    ButtonWidget equipButton = ButtonWidget.builder(Text.literal("Equip [" + ExtraInventoryUtils.getSetName(setId) + "]"), buttonWidget -> {
                        ExtraInventoryUtils.equipSet(containerScreen.getScreenHandler().slots, setId);
                    }).dimensions(equipButtonX, equipButtonY, 96, 22).build();

                    // Store the equip button reference for later update
                    equipButtonRef[0] = equipButton;

                    screen.addDrawableChild(setNameTextbox);
                    Screens.getButtons(containerScreen).add(renameSaveButton);
                    Screens.getButtons(containerScreen).add(equipButton);
                }
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
