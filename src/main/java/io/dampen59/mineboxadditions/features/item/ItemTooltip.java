package io.dampen59.mineboxadditions.features.item;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.utils.SocketManager;
import io.dampen59.mineboxadditions.utils.Utils;
import io.dampen59.mineboxadditions.features.item.Insect;
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
import java.util.stream.Collectors;

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
            List<MineboxItem> mbxItems = MineboxAdditions.INSTANCE.state.getMbxItems();
            if (mbxItems == null || mbxItems.isEmpty()) return;
            MineboxItem mbxItem = Utils.findItemByName(mbxItems, itemId);

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
                                        newNestedSiblings.add(buildRollRange(stat));
                                        modified = true;
                                        continue;
                                    }
                                }
                            }

                            String resistanceKey = getResistanceStatKey(nestedSibling);
                            if (resistanceKey != null) {
                                MineboxStat stat = mbxItem.getStat(resistanceKey).orElse(null);
                                if (stat != null && stat.getMin() != null && stat.getMax() != null) {
                                    newNestedSiblings.add(buildRollRange(stat));
                                    modified = true;
                                    continue;
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

            Insect insect = MineboxAdditions.INSTANCE.state.getInsectById(itemId);
            if (insect != null) {
                lines.add(Component.literal(""));
                lines.add(Component.literal("Spawn Conditions").withColor(0xFFD700));

                String timeStr = insect.getTimeRanges().stream()
                        .map(Insect.TimeRange::toString)
                        .collect(Collectors.joining(", "));
                lines.add(Component.literal("Time: ").withColor(0x4497CE)
                        .append(Component.literal(timeStr).withColor(0xFFFFFF)));

                lines.add(Component.literal("Weather: ").withColor(0x4497CE)
                        .append(Component.literal(insect.getWeather().display()).withColor(0xFFFFFF)));

                if (insect.requiresMoon()) {
                    lines.add(Component.literal("Moon: ").withColor(0x4497CE)
                            .append(Component.literal("Full / New Moon").withColor(0xFFFFFF)));
                }

                if (!insect.getLocations().isEmpty()) {
                    lines.add(Component.literal("Locations:").withColor(0x4497CE));
                    for (var loc : insect.getLocations()) {
                        lines.add(Component.literal("  • ")
                                .withColor(0xAAAAAA)
                                .append(Component.translatable(loc.getZone()).withColor(0xAAAAAA))
                                .append(Component.literal(" - ").withColor(0xAAAAAA))
                                .append(Component.translatable(loc.getSubarea()).withColor(0xAAAAAA)));
                    }
                }
            }

            lines.add(Component.literal(""));
            lines.add(Component.literal("Minebox ID: ").withColor(0x4497CE)
                    .append(Component.literal(itemId).withColor(0x1D4159)));
        } else {
            lines.add(Component.literal(""));

            Component firstPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.press").withColor(0x4497CE);
            Component midPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.key").withColor(0x1D4159);
            Component endPart = Component.translatable("mineboxadditions.strings.tooltip.more_info.desc").withColor(0x4497CE);

            Component moreInfos = firstPart.copy().append(midPart).append(endPart);
            lines.add(moreInfos);
        }
    }

    private static String getResistanceStatKey(Component component) {
        String element = null;
        boolean hasResistance = false;
        for (Component child : component.getSiblings()) {
            if (child.getContents() instanceof TranslatableContents translatableContent) {
                String key = translatableContent.getKey();
                if (key.startsWith("mbx.elements.")) {
                    element = key.substring("mbx.elements.".length());
                } else if (key.equals("mbx.resistance")) {
                    hasResistance = true;
                }
            }
        }
        return (element != null && hasResistance) ? "mbx_stats_" + element + "_resistance" : null;
    }

    private static Component buildRollRange(MineboxStat stat) {
        int minRoll = stat.getMin();
        int maxRoll = stat.getMax();

        ChatFormatting color = (minRoll < 0 && maxRoll < 0)
                ? ChatFormatting.RED
                : ChatFormatting.DARK_GREEN;

        String numericRange = (minRoll == maxRoll)
                ? " [" + maxRoll + "]"
                : " [" + minRoll + " to " + maxRoll + "]";

        return Component.literal(numericRange).setStyle(Style.EMPTY.withColor(color));
    }
}
