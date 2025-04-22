package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "setOverlayMessage", at = @At("TAIL"))
    private void onActionBarMessage(Text message, boolean overlay, CallbackInfo ci) {
        MinecraftClient.getInstance().execute(() -> {
            String messageString = message.getString();

            // 끳 = Luxs
            // 끱 = Gems
            if (!messageString.contains("끳") || !messageString.contains("끱")) return;

            MineboxAdditionsClient.INSTANCE.modState.setChatLang(messageString);
        });
    }
}