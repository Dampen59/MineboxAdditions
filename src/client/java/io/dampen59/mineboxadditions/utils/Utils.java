package io.dampen59.mineboxadditions.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenHandler;

public class Utils {

    public static void showToastNotification(String prmTitle, String prmDescription) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.getToastManager().add(
                new SystemToast(
                        SystemToast.Type.PERIODIC_NOTIFICATION,
                        Text.literal(prmTitle),
                        Text.literal(prmDescription)
                )
        );
    }

    public static void playSound(SoundEvent prmSound) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        client.player.playSound(
                prmSound,
                1.0f,
                1.0f
        );

    }

    public static String getInventoryName() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen  == null) return null;
        Screen currentScreen = client.currentScreen;
        System.out.println(currentScreen.getTitle());
        return "lol";
    }
}
