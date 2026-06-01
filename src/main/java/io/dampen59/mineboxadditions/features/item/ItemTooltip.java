package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ItemTooltip {
    private static final int TOOLTIP_KEY = InputConstants.KEY_LALT;

    public static void init() {
        ItemTooltipCallback.EVENT.register(ItemTooltip::handle);
    }

    private static void handle(ItemStack item, Item.TooltipContext context, TooltipFlag type, List<Component> lines) {
        if (!Utils.isMineboxItem(item)) return;

        boolean isKeyPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), TOOLTIP_KEY);
        if (isKeyPressed) {
            String itemId = Utils.getMineboxItemId(item);
            if (MineboxAdditions.INSTANCE.state.getMbxItems().isEmpty()) return;
            MineboxItem mbxItem = Utils.findItemByName(MineboxAdditions.INSTANCE.state.getMbxItems(), itemId);

            if (mbxItem != null && !mbxItem.getMbxStats().isEmpty()) {
                for (int i = 0; i < lines.size(); i++) {
                    Component originalText = lines.get(i);
                    boolean modified = false;
                    List<Component> updatedSiblings = new ArrayList<>();

                    for (Component sibling : originalText.getSiblings()) {
                        List<Component> newNestedSiblings = new ArrayList<>();

                        for (Component nestedSibling : sibling.getSiblings()) {
                            if (nestedSibling.getContents() instanceof TranslatableContents translatableContent) {
                                String translationKey = translatableContent.getKey();
                                if (translationKey.startsWith("mbx.stats.")) {
                                    String jsonKey = translationKey.replace(".", "_");
                                    MineboxStat stat = mbxItem.getStat(jsonKey).orElse(null);
                                    if (stat != null && stat.getMin() != null && stat.getMax() != null) {
                                        int minRoll = stat.getMin();
                                        int maxRoll = stat.getMax();

                                        ChatFormatting color = (minRoll < 0 && maxRoll < 0)
                                                ? ChatFormatting.RED
                                                : ChatFormatting.DARK_GREEN;

                                        String numericRange = (minRoll == maxRoll)
                                                ? " [" + maxRoll + "]"
                                                : " [" + minRoll + " to " + maxRoll + "]";

                                        newNestedSiblings.add(
                                                Component.literal(numericRange)
                                                        .setStyle(Style.EMPTY.withColor(color))
                                        );
                                        modified = true;
                                        continue;
                                    }
                                }
                            }
                            newNestedSiblings.add(nestedSibling);
                        }

                        MutableComponent updatedSibling;
                        if (sibling.getContents() instanceof TranslatableContents translatableContent
                                && translatableContent.getKey().startsWith("mbx.stats.")) {
                            updatedSibling = Component.literal("");
                        } else {
                            updatedSibling = sibling.copy();
                        }
                        for (Component nested : newNestedSiblings) {
                            updatedSibling = updatedSibling.append(nested);
                        }
                        updatedSiblings.add(updatedSibling);
                    }

                    if (modified) {
                        MutableComponent updatedText = Component.literal("");
                        for (Component sib : updatedSiblings) {
                            updatedText = updatedText.append(sib);
                        }
                        lines.set(i, updatedText);
                    }
                }
            }

            lines.add(Component.literal(""));

            Component firstPart = Component.literal("Minebox ID: ").withColor(0x4497CE);
            Component endPart = Component.literal(itemId).withColor(0x1D4159);
            Component mineboxItemId = firstPart.copy().append(endPart);
            lines.add(mineboxItemId);
        } else {
            lines.add(Component.literal(""));

            Component firstPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.press").withColor(0x4497CE);
            Component midPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.key").withColor(0x1D4159);
            Component endPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.desc").withColor(0x4497CE);

            Component moreInfos = firstPart.copy().append(midPart).append(endPart);
            lines.add(moreInfos);
        }
    }
}
