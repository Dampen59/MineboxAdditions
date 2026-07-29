package io.dampen59.mineboxadditions.features.hud.huds.haversack;

import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public class HaversackManager {
    private int lastAmountInside = -1;
    private double fillRatePerSecond = 0.0;
    private String timeUntilFull = "";
    private long lastCheckTime = System.currentTimeMillis();

    public HaversackManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handle);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath("mineboxadditions", "haversack"), this::render);
    }

    private void handle(Minecraft client) {
        if (client.player == null || client.level == null || !Utils.isOnMinebox()) return;
        ItemStack offHandStack = client.player.getOffhandItem();
        String offHandStackId = Utils.getMineboxItemId(offHandStack);
        if (offHandStackId != null && offHandStackId.startsWith("haversack_")) {
            handleStack(offHandStack);
        } else {
            reset();
        }
    }

    private void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        if (fillRatePerSecond != 0) {
            var rate = HudManager.INSTANCE.get(HaversackHud.RateHud.class);
            rate.update(fillRatePerSecond);
            if (rate.getState()) rate.draw(context);

            var full = HudManager.INSTANCE.get(HaversackHud.FullHud.class);
            full.update(timeUntilFull);
            if (full.getState()) full.draw(context);
        }
    }

    private void handleStack(ItemStack stack) {
        var itemData = stack.get(DataComponents.CUSTOM_DATA);
        if (itemData == null) return;
        CompoundTag nbtData = itemData.copyTag();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;
        String id = nbtData.getString("mbitems:id").orElse("");
        ItemLore loreComponent = stack.get(DataComponents.LORE);
        if (loreComponent == null) return;

        for (Component lore : loreComponent.lines()) {
            if (!(lore.getContents() instanceof TranslatableContents translatableContent)) continue;
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                parseInformation(stack, nbtData, translatableContent);
            }
        }
    }

    private void parseInformation(ItemStack stack, CompoundTag nbtData, TranslatableContents content) {
        Object arg = content.getArgs()[0];
        if (!(arg instanceof Component argComponent)) return;
        String quantityStr = argComponent.getString();
        String[] parts = quantityStr.split("/");
        if (parts.length < 2) return;
        int maxQuantity = Integer.parseInt(parts[1]);
        CompoundTag persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
        int amountInside = persistentData.getInt("mbitems:amount_inside").orElse(0);
        long currentTime = System.currentTimeMillis();
        if (lastAmountInside >= 0) {
            long deltaTime = currentTime - lastCheckTime;
            if (deltaTime >= 1000) {
                int deltaAmount = amountInside - lastAmountInside;
                fillRatePerSecond = deltaAmount / (deltaTime / 1000.0);
                lastCheckTime = currentTime;
                lastAmountInside = amountInside;

                // Estimate time to full
                int remaining = maxQuantity - amountInside;
                if (fillRatePerSecond > 0) {
                    long secondsLeft = (long) (remaining / fillRatePerSecond);
                    timeUntilFull = Utils.formatTime(secondsLeft);
                } else {
                    timeUntilFull = "∞";
                }
            }
        } else {
            lastAmountInside = amountInside;
            lastCheckTime = currentTime;
            timeUntilFull = "";
        }
    }

    private void reset() {
        lastAmountInside = -1;
        fillRatePerSecond = 0.0;
        timeUntilFull = "";
    }
}
