package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShopEvent {

    private final Shop shop;
    private final State modState;
    private boolean isShopOpen = false;

    private final BooleanSupplier isAlertSent;
    private final Consumer<Boolean> setAlertSent;
    private final Supplier<String> getCurrentItemOffer;
    private final Consumer<String> setCurrentItemOffer;
    private final BooleanSupplier isConfigEnabled;

    public ShopEvent(Shop shop,
                     State modState,
                     BooleanSupplier isAlertSent,
                     Consumer<Boolean> setAlertSent,
                     Supplier<String> getCurrentItemOffer,
                     Consumer<String> setCurrentItemOffer,
                     BooleanSupplier isConfigEnabled) {
        this.shop = shop;
        this.modState = modState;
        this.isAlertSent = isAlertSent;
        this.setAlertSent = setAlertSent;
        this.getCurrentItemOffer = getCurrentItemOffer;
        this.setCurrentItemOffer = setCurrentItemOffer;
        this.isConfigEnabled = isConfigEnabled;

        HudRenderCallback.EVENT.register(this::onRenderHud);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (!modState.getConnectedToMinebox()) return;
        if (client.world == null) return;

        long currentWorldTicks = client.world.getTimeOfDay() % 24000;
        if (currentWorldTicks >= shop.getStartTime() && currentWorldTicks <= shop.getStopTime()) {
            isShopOpen = true;
            if (isConfigEnabled.getAsBoolean() && !isAlertSent.getAsBoolean()) {
                Utils.showShopToastNotification(
                        shop.name(),
                        Text.translatable(shop.getToastTitleKey()).getString(),
                        Text.translatable(shop.getToastContentKey()).getString());
                Utils.playSound(SoundEvents.BLOCK_BELL_USE);
                setAlertSent.accept(true);
            }
        } else {
            isShopOpen = false;
            if (isAlertSent.getAsBoolean()) {
                setAlertSent.accept(false);
            }
            if (getCurrentItemOffer.get() != null) {
                setCurrentItemOffer.accept(null);
            }
        }
    }

    private void onRenderHud(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;
        if (client.player == null) return;

        String displayOffer = getCurrentItemOffer.get();
        if (displayOffer != null) {
            drawContext.drawText(client.textRenderer, Text.of(displayOffer), 5, 40, 0xFFFFFF, true);
        } else if (isShopOpen) {
            drawContext.drawText(client.textRenderer, Text.translatable(shop.getToastTitleKey()), 5, 40, 0xFFFFFF, true);
        }
    }
}
