package io.dampen59.mineboxadditions.features.shop;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.notifications.NotificationsConfig;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.util.function.Supplier;

public enum Shop {
    BUCKSTAR(LocalTime.parse("06:00"), LocalTime.parse("12:00"),
            () -> NotificationsConfig.shop.buckstarToast || NotificationsConfig.shop.buckstarBell,
            null),
    BAKERY(LocalTime.parse("12:00"), LocalTime.parse("18:00"),
            () -> NotificationsConfig.shop.bakeryToast || NotificationsConfig.shop.bakeryBell,
            null),
    SHARKOFFE(LocalTime.parse("18:00"), LocalTime.parse("19:30"),
            () -> NotificationsConfig.shop.sharkoffeToast || NotificationsConfig.shop.sharkoffeBell,
            null),
    MOUSE(LocalTime.parse("19:30"), LocalTime.parse("02:00"),
            () -> NotificationsConfig.shop.mouseToast || NotificationsConfig.shop.mouseBell,
            null),
    REGGAE_DEALER(LocalTime.parse("19:30"), LocalTime.parse("02:00"),
            () -> NotificationsConfig.shop.reggaeDealerToast || NotificationsConfig.shop.reggaeDealerBell,
            () -> MineboxAdditions.INSTANCE.state.getCurrentMoonPhase() == 0),
    PAINTINGS_SELLER(LocalTime.parse("18:00"), LocalTime.parse("19:30"),
            () -> NotificationsConfig.shop.paintingsSellerToast || NotificationsConfig.shop.paintingsSellerBell,
            null),
    SUSHI_SELLER(LocalTime.parse("18:00"), LocalTime.parse("19:30"),
            () -> NotificationsConfig.shop.sushiSellerToast || NotificationsConfig.shop.sushiSellerBell,
            null);

    private final LocalTime start;
    private final LocalTime end;
    private final Supplier<Boolean> notifState;
    private final Supplier<Boolean> extraCondition;
    private boolean alerted = false;
    private Component offer;

    Shop(LocalTime start, LocalTime end, Supplier<Boolean> notifState, Supplier<Boolean> extraCondition) {
        this.start = start;
        this.end = end;
        this.notifState = notifState;
        this.extraCondition = extraCondition;
    }

    public Component getName() {
        return Component.translatable("mineboxadditions." + this.name().toLowerCase());
    }

    public boolean isEnabled() {
        return notifState.get();
    }

    public boolean isOpen() {
        LocalTime server = Utils.getTime();
        boolean timeOk;
        if (end.isBefore(start)) {
            timeOk = !server.isBefore(start) || !server.isAfter(end);
        } else {
            timeOk = !server.isBefore(start) && !server.isAfter(end);
        }
        if (!timeOk) return false;
        return extraCondition == null
                || (MineboxAdditions.INSTANCE != null && extraCondition.get());
    }

    public boolean isAlerted()              { return alerted; }
    public void setAlerted(boolean alerted) { this.alerted = alerted; }
    public Component getOffer()             { return offer; }

    public void setOffer(String offer) {
        this.offer = Component.translatable(offer);
    }

    public void reset() {
        this.alerted = false;
        this.offer = null;
    }
}
