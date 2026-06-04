package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.config.huds.HudsConfig;
import io.dampen59.mineboxadditions.config.notifications.NotificationsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.util.function.Supplier;

public enum Shop {
    BUCKSTAR(LocalTime.parse("06:00"), LocalTime.parse("12:00"), () -> NotificationsConfig.shop.buckstarToast || NotificationsConfig.shop.buckstarBell),
    BAKERY(LocalTime.parse("12:00"), LocalTime.parse("18:00"), () -> NotificationsConfig.shop.bakeryToast || NotificationsConfig.shop.bakeryBell),
    SHARKOFFE(LocalTime.parse("18:00"), LocalTime.parse("19:30"), () -> NotificationsConfig.shop.sharkoffeToast || NotificationsConfig.shop.sharkoffeBell),
    MOUSE(LocalTime.parse("19:30"), LocalTime.parse("02:00"), () -> NotificationsConfig.shop.mouseToast || NotificationsConfig.shop.mouseBell);

    private final Supplier<Boolean> state;
    private final LocalTime start;
    private final LocalTime end;
    private boolean alerted = false;
    private Component offer;

    Shop(LocalTime start, LocalTime end, Supplier<Boolean> state) {
        this.start = start;
        this.end = end;
        this.state = state;
    }

    public Component getName() {
        return Component.translatable("mineboxadditions." + this.name().toLowerCase());
    }

    public boolean isEnabled() {
        return state.get();
    }

    public boolean isOpen() {
        LocalTime server = Utils.getTime();
        if (end.isBefore(start)) return !server.isBefore(start) || !server.isAfter(end);
        return !server.isBefore(start) && !server.isAfter(end);
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void setAlerted(boolean alerted) {
        this.alerted = alerted;
    }

    public Component getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = Component.translatable(offer);
    }

    public void reset() {
        this.alerted = false;
        this.offer = null;
    }
}
