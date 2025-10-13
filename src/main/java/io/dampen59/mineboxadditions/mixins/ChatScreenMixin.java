package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.MineboxAdditions;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (chatText.startsWith("/")) {
            MineboxAdditions.INSTANCE.state.setLastSentCommand(chatText);
        }
    }
}



