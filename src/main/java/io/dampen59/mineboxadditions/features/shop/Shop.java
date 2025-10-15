package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public enum Shop {
    BUCKSTAR(10, 6000, () -> HudsConfig.shop.buckstar),
    BAKERY(6000, 12000, () -> HudsConfig.shop.bakery),
    MOUSE(12000, 13500, () -> HudsConfig.shop.mouse),
    SHARKOFFE(13500, 23000, () -> HudsConfig.shop.sharkoffe);

    private final Supplier<Boolean> state;
    private final int start;
    private final int end;
    private boolean alerted = false;
    private Text offer;

    Shop(int start, int end, Supplier<Boolean> state) {
        this.start = start;
        this.end = end;
        this.state = state;
    }

    public Text getName() {
        return Text.translatable("mineboxadditions." + this.name().toLowerCase());
    }

    public boolean isEnabled() {
        return state.get();
    }

    public boolean isOpen(long time) {
        return time >= start && time <= end;
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void setAlerted(boolean alerted) {
        this.alerted = alerted;
    }

    public Text getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = Text.translatable(offer);
    }

    public void reset() {
        this.alerted = false;
        this.offer = null;
    }
}
