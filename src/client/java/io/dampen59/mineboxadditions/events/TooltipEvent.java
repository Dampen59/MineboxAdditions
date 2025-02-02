package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.classes.MineboxItem;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.ArrayList;
import java.util.List;

public class TooltipEvent {
    private State modState = null;
    private static final int LEFT_ALT_KEY = InputUtil.GLFW_KEY_LEFT_ALT;

    public TooltipEvent(State prmModState) {
        this.modState = prmModState;
        initializeTooltips();
    }

    public void initializeTooltips() {
        ItemTooltipCallback.EVENT.register(this::onTooltip);
    }

    private void onTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> texts) {

        boolean isAltPressed = InputUtil.isKeyPressed(
                MinecraftClient.getInstance().getWindow().getHandle(), LEFT_ALT_KEY
        );

        if (!Utils.isMineboxItem(itemStack)) return;
        if (!Utils.itemHaveStats(itemStack)) return;

        if (isAltPressed) {

            String itemId = Utils.getMineboxItemId(itemStack);

            MineboxItem mbxItem = Utils.findItemByName(modState.getMbxItems(), itemId);
            if (mbxItem == null) return;

            for (int i = 0; i < texts.size(); i++) {
                Text text = texts.get(i);
                boolean hasBeenModified = false;

                List<Text> updatedSiblings = new ArrayList<>();

                for (Text sibling : text.getSiblings()) {
                    updatedSiblings.add(sibling);
                    List<Text> nestedSiblings = sibling.getSiblings();
                    for (Text nestedSibling : nestedSiblings) {
                        if (nestedSibling.getContent() instanceof TranslatableTextContent) {
                            TranslatableTextContent translatableContent = (TranslatableTextContent) nestedSibling.getContent();
                            String translationKey = translatableContent.getKey();
                            if (translationKey.contains("mbx.stats.")) {
                                String jsonKey = translationKey.replace(".", "_");
                                int minRoll = mbxItem.getStats(jsonKey).getMin();
                                int maxRoll = mbxItem.getStats(jsonKey).getMax();
                                String baseStats = null;
                                if (minRoll == maxRoll) {
                                    baseStats = " [" + maxRoll + "]";
                                } else {
                                    baseStats = " [" + minRoll + " - " +  maxRoll + "]";
                                }
                                Text baseStatsSibling = Text.literal(baseStats).setStyle(sibling.getStyle());
                                updatedSiblings.add(baseStatsSibling);
                                hasBeenModified = true;
                                break;
                            }
                        }
                    }
                }

                if (hasBeenModified) {
                    Text updatedText = Text.literal("");
                    for (Text updatedSibling : updatedSiblings) {
                        updatedText = updatedText.copy().append(updatedSibling);
                    }
                    texts.set(i, updatedText);
                }
            }

        } else {
            texts.add(Text.of(""));
            texts.add(Text.translatable("mineboxadditions.strings.tooltip.more_info"));
        }
    }
}
