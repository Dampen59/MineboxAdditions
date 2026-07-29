package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.MineboxAdditions;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"))
    private void mbx$sendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (chatText.startsWith("/")) {
            MineboxAdditions.INSTANCE.state.setLastSentCommand(chatText);
        }
    }
}



