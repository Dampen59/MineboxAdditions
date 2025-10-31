package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ItemTooltip {
    private static final int TOOLTIP_KEY = InputUtil.GLFW_KEY_LEFT_ALT;

    public static void init() {
        ItemTooltipCallback.EVENT.register(ItemTooltip::handle);
    }

    private static void handle(ItemStack item, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        if (!Utils.isMineboxItem(item)) return;

        boolean isKeyPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), TOOLTIP_KEY);
        if (isKeyPressed) {
            String itemId = Utils.getMineboxItemId(item);
            if (MineboxAdditions.INSTANCE.state.getMbxItems().isEmpty()) return;
            MineboxItem mbxItem = Utils.findItemByName(MineboxAdditions.INSTANCE.state.getMbxItems(), itemId);

            if (mbxItem != null && !mbxItem.getMbxStats().isEmpty()) {
                for (int i = 0; i < lines.size(); i++) {
                    Text originalText = lines.get(i);
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
                        lines.set(i, updatedText);
                    }
                }
            }

            lines.add(Text.literal(""));

            Text firstPart = Text.literal("Minebox ID: ").withColor(0x4497CE);
            Text endPart = Text.literal(itemId).withColor(0x1D4159);
            Text mineboxItemId = firstPart.copy().append(endPart);
            lines.add(mineboxItemId);
        } else {
            lines.add(Text.literal(""));

            Text firstPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.press").withColor(0x4497CE);
            Text midPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.key").withColor(0x1D4159);
            Text endPart = Text.translatable("mineboxadditions.strings.tooltip.more_info.desc").withColor(0x4497CE);

            Text moreInfos = firstPart.copy().append(midPart).append(endPart);
            lines.add(moreInfos);
        }
    }
}
