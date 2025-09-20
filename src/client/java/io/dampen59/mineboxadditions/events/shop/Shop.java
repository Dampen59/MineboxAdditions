package io.dampen59.mineboxadditions.events.shop;

public enum Shop {
    BAKERY(6000, 12000, "mineboxadditions.strings.toasts.shop.bakery.open.title", "mineboxadditions.strings.toasts.shop.bakery.open.content"),
    BUCKSTAR(10, 6000, "mineboxadditions.strings.toasts.shop.buckstar.open.title", "mineboxadditions.strings.toasts.shop.buckstar.open.content"),
    COCKTAIL(12000, 13500, "mineboxadditions.strings.toasts.shop.cocktail.open.title", "mineboxadditions.strings.toasts.shop.cocktail.open.content"),
    MOUSE(13500, 23000, "mineboxadditions.strings.toasts.shop.mouse.open.title", "mineboxadditions.strings.toasts.shop.mouse.open.content");

    private final int startTime;
    private final int stopTime;
    private final String toastTitleKey;
    private final String toastContentKey;

    Shop(int startTime, int stopTime, String toastTitleKey, String toastContentKey) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.toastTitleKey = toastTitleKey;
        this.toastContentKey = toastContentKey;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getStopTime() {
        return stopTime;
    }

    public String getToastTitleKey() {
        return toastTitleKey;
    }

    public String getToastContentKey() {
        return toastContentKey;
    }
}
