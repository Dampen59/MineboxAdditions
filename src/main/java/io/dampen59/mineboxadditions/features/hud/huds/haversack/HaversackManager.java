package io.dampen59.mineboxadditions.features.hud.huds.haversack;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
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
    private long lastMonoTimeNs = -1L;
    private double fillRatePerSecond = 0.0;
    private String timeUntilFull = "";
    private static final long MIN_INTERVAL_NS = 300_000_000L;
    private static final double EMA_ALPHA = 0.3;

    public HaversackManager() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handle);
        HudRenderCallback.EVENT.register(this::render);
    }

    private void handle(MinecraftClient client) {
        if (client.player == null || client.world == null || !Utils.isOnMinebox()) return;

        Stream.of(client.player.getOffHandStack())
                .filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);
        client.player.getInventory().getMainStacks().stream().filter(stack -> !stack.isEmpty())
                .forEach(this::handleDurability);

        if (client.currentScreen instanceof GenericContainerScreen containerScreen) {
            containerScreen.getScreenHandler().slots.forEach(slot -> {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    handleDurability(stack);
                }
            });
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
        String[] parts = content.getArg(0).getString().split("/");
        if (parts.length < 2) return;
        int maxQuantity;
        try {
            maxQuantity = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) { return; }

        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent").orElse(null);
        if (persistentData == null) return;
        int amountInside = persistentData.getInt("mbitems:amount_inside").orElse(-1);
        if (amountInside < 0) return;

        long now = System.nanoTime();
        if (lastMonoTimeNs < 0 || lastAmountInside < 0) {
            lastMonoTimeNs = now;
            lastAmountInside = amountInside;
            timeUntilFull = "";
            return;
        }

        long dtNs = now - lastMonoTimeNs;
        if (dtNs < MIN_INTERVAL_NS) {
            return;
        }

        int dAmount = amountInside - lastAmountInside;

        if (dAmount <= 0 || dtNs <= 0) {
            lastMonoTimeNs = now;
            lastAmountInside = amountInside;
            fillRatePerSecond = Math.max(0.0, fillRatePerSecond * 0.8);
            timeUntilFull = fillRatePerSecond > 0
                    ? Utils.formatTime((long)((maxQuantity - amountInside) / fillRatePerSecond))
                    : "∞";
            return;
        }

        double dtSec = dtNs / 1_000_000_000.0;
        double instantRate = dAmount / dtSec;

        fillRatePerSecond = EMA_ALPHA * instantRate + (1.0 - EMA_ALPHA) * fillRatePerSecond;

        lastMonoTimeNs = now;
        lastAmountInside = amountInside;

        int remaining = Math.max(0, maxQuantity - amountInside);
        if (fillRatePerSecond > 0.0001 && Double.isFinite(fillRatePerSecond)) {
            long secondsLeft = (long) Math.ceil(remaining / fillRatePerSecond);
            timeUntilFull = Utils.formatTime(secondsLeft);
        } else {
            timeUntilFull = "∞";
        }
    }
}
