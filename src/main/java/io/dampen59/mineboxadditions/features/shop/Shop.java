package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.util.function.Supplier;

public enum Shop {
    BUCKSTAR(LocalTime.parse("06:00"), LocalTime.parse("12:00"), () -> HudsConfig.shop.buckstar),
    BAKERY(LocalTime.parse("12:00"), LocalTime.parse("18:00"), () -> HudsConfig.shop.bakery),
    SHARKOFFE(LocalTime.parse("18:00"), LocalTime.parse("17:30"), () -> HudsConfig.shop.sharkoffe),
    MOUSE(LocalTime.parse("17:30"), LocalTime.parse("02:00"), () -> HudsConfig.shop.mouse);

    private final Supplier<Boolean> state;
    private final LocalTime start;
    private final LocalTime end;
    private boolean alerted = false;
    private Text offer;

    Shop(LocalTime start, LocalTime end, Supplier<Boolean> state) {
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

    public boolean isOpen() {
        LocalTime server = Utils.getTime();
        return (server.equals(start) || server.isAfter(start)) &&
                (server.equals(end) || server.isBefore(end));
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
