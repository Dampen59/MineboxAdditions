package io.dampen59.mineboxadditions.features.hud.huds.haversack;

import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.stream.Stream;

public class HaversackManager {
    private int lastAmountInside = -1;
    private double fillRatePerSecond = 0.0;
    private String timeUntilFull = "";
    private long lastCheckTime = System.currentTimeMillis();

    public HaversackManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handle);
        HudRenderCallback.EVENT.register(this::render);
    }

    private void handle(MinecraftClient client) {
        if (client.player == null || client.world == null || !Utils.isOnMinebox()) return;
        ItemStack offHandStack = client.player.getOffHandStack();
        String offHandStackId = Utils.getMineboxItemId(offHandStack);
        if (offHandStackId != null && offHandStackId.startsWith("haversack_")) {
            handleDurability(offHandStack);
        } else {
            reset();
        }
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        if (fillRatePerSecond != 0) {
            var rate = HudManager.INSTANCE.get(HaversackHud.RateHud.class);
            rate.update(fillRatePerSecond);
            if (rate.getState()) rate.draw(context);

            var full = HudManager.INSTANCE.get(HaversackHud.FullHud.class);
            full.update(timeUntilFull);
            if (full.getState()) full.draw(context);
        }
    }

    private void handleDurability(ItemStack stack) {
        var itemData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return;
        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;
        String id = nbtData.getString("mbitems:id").orElse("");
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) return;

        for (Text lore : loreComponent.lines()) {
            if (!(lore.getContent() instanceof TranslatableTextContent translatableContent)) continue;
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                handleHaversackDurability(stack, nbtData, translatableContent);
            }
        }
    }

    private void handleHaversackDurability(ItemStack stack, NbtCompound nbtData, TranslatableTextContent content) {
        StringVisitable quantityArg = content.getArg(0);
        String[] parts = quantityArg.getString().split("/");
        if (parts.length < 2) return;
        int maxQuantity = Integer.parseInt(parts[1]);
        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
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
