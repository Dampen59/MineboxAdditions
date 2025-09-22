package io.dampen59.mineboxadditions.events.shop;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.hud.Hud;
import io.dampen59.mineboxadditions.state.HUDState;
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
    private final State state;

    private final BooleanSupplier isAlertSent;
    private final Consumer<Boolean> setAlertSent;
    private final Supplier<String> getCurrentOffer;
    private final Consumer<String> setCurrentOffer;
    private final BooleanSupplier isConfigEnabled;

    private boolean isShopOpen = false;

    public ShopEvent(
            Shop shop,
            State state,
            BooleanSupplier isAlertSent,
            Consumer<Boolean> setAlertSent,
            Supplier<String> getCurrentOffer,
            Consumer<String> setCurrentOffer,
            BooleanSupplier isConfigEnabled
    ) {
        this.shop = shop;
        this.state = state;
        this.isAlertSent = isAlertSent;
        this.setAlertSent = setAlertSent;
        this.getCurrentOffer = getCurrentOffer;
        this.setCurrentOffer = setCurrentOffer;
        this.isConfigEnabled = isConfigEnabled;

        HudRenderCallback.EVENT.register(this::onRenderHud);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (!state.isConnectedToMinebox() || client.world == null) return;

        long worldTime = client.world.getTimeOfDay() % 24000;
        boolean withinShopTime = worldTime >= shop.getStartTime() && worldTime <= shop.getStopTime();

        if (withinShopTime) {
            handleShopOpen();
        } else {
            handleShopClosed();
        }
    }

    private void handleShopOpen() {
        isShopOpen = true;

        if (isConfigEnabled.getAsBoolean() && !isAlertSent.getAsBoolean()) {
            showShopAlert();
        }
    }

    private void handleShopClosed() {
        isShopOpen = false;

        if (isAlertSent.getAsBoolean()) {
            setAlertSent.accept(false);
        }

        if (getCurrentOffer.get() != null) {
            setCurrentOffer.accept(null);
        }
    }

    private void showShopAlert() {
        Utils.showShopToastNotification(
                shop.name(),
                Text.translatable(shop.getToastTitleKey()).getString(),
                Text.translatable(shop.getToastContentKey()).getString()
        );
        Utils.playSound(SoundEvents.BLOCK_BELL_USE);
        setAlertSent.accept(true);
    }

    private void onRenderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;

        HUDState hudState = MineboxAdditionsClient.INSTANCE.modState.getHUDState();
        Hud hud = hudState.getHud(Hud.Type.SHOP);
        String offer = getCurrentOffer.get();
        if (offer != null) {
            hud.setText(Text.of(offer));
            hud.draw(context);
        } else if (isShopOpen) {
            hud.setText(Text.translatable(shop.getToastTitleKey()));
            hud.draw(context);
        }
    }
}