package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.minebox.MineboxItem;
import io.dampen59.mineboxadditions.minebox.MineboxStat;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TooltipEvent {
    private final State modState;
    private static final int LEFT_ALT_KEY = InputUtil.GLFW_KEY_LEFT_ALT;

    public TooltipEvent(State modState) {
        this.modState = modState;
        initializeTooltips();
    }

    private void initializeTooltips() {
        ItemTooltipCallback.EVENT.register(this::onTooltip);
    }

    private void onTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> texts) {

        //if (!Utils.isMineboxItem(itemStack) || !Utils.itemHaveStats(itemStack)) return;
        if (!Utils.isMineboxItem(itemStack)) return;

        boolean isAltPressed = InputUtil.isKeyPressed(
                MinecraftClient.getInstance().getWindow().getHandle(), LEFT_ALT_KEY
        );

        if (isAltPressed) {
            String itemId = Utils.getMineboxItemId(itemStack);

            if (modState.getMbxItems() == null) {
                System.out.println("[MineboxAdditions] Cannot display more info because modState.getMbxItems() is null");
                return;
            }

            MineboxItem mbxItem = Utils.findItemByName(modState.getMbxItems(), itemId);
            //if (mbxItem == null) return;

            for (int i = 0; i < texts.size(); i++) {
                Text originalText = texts.get(i);
                boolean modified = false;
                List<Text> updatedSiblings = new ArrayList<>();

                for (Text sibling : originalText.getSiblings()) {
                    List<Text> newNestedSiblings = new ArrayList<>();

                    for (Text nestedSibling : sibling.getSiblings()) {
                        if (nestedSibling.getContent() instanceof TranslatableTextContent translatableContent) {
                            String translationKey = translatableContent.getKey();
                            if (translationKey.startsWith("mbx.stats.")) {
                                String jsonKey = translationKey.replace(".", "_");
                                MineboxStat stat = mbxItem.getStat(jsonKey).orElse(null);
                                if (stat != null && stat.getMin() != null && stat.getMax() != null) {
                                    int minRoll = stat.getMin();
                                    int maxRoll = stat.getMax();

                                    Formatting color = (minRoll < 0 && maxRoll < 0)
                                            ? Formatting.RED
                                            : Formatting.DARK_GREEN;

                                    String numericRange = (minRoll == maxRoll)
                                            ? " [" + maxRoll + "]"
                                            : " [" + minRoll + " to " + maxRoll + "]";

                                    newNestedSiblings.add(
                                            Text.literal(numericRange)
                                                    .setStyle(Style.EMPTY.withColor(color))
                                    );
                                    modified = true;
                                    continue;
                                }
                            }
                        }
                        newNestedSiblings.add(nestedSibling);
                    }

                    MutableText updatedSibling;
                    if (sibling.getContent() instanceof TranslatableTextContent translatableContent
                            && translatableContent.getKey().startsWith("mbx.stats.")) {
                        updatedSibling = Text.literal("");
                    } else {
                        updatedSibling = sibling.copy();
                    }
                    for (Text nested : newNestedSiblings) {
                        updatedSibling = updatedSibling.append(nested);
                    }
                    updatedSiblings.add(updatedSibling);
                }

                if (modified) {
                    MutableText updatedText = Text.literal("");
                    for (Text sib : updatedSiblings) {
                        updatedText = updatedText.append(sib);
                    }
                    texts.set(i, updatedText);
                }
            }

            texts.add(Text.literal(""));

            Text firstPart = Text.literal("Minebox ID: ").withColor(0x4497CE);
            Text endPart = Text.literal(itemId).withColor(0x1D4159);
            Text mineboxItemId = firstPart.copy().append(endPart);
            texts.add(mineboxItemId);

        } else {
            texts.add(Text.literal(""));

            Text firstPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.press").withColor(0x4497CE);
            Text midPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.key").withColor(0x1D4159);
            Text endPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.desc").withColor(0x4497CE);

            Text moreInfos = firstPart.copy().append(midPart).append(endPart);
            texts.add(moreInfos);
        }
    }
}
