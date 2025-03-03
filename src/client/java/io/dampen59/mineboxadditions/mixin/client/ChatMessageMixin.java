package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.minebox.ParsedMessage;
import io.dampen59.mineboxadditions.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMessageMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onChatMessageReceived(GameMessageS2CPacket packet, CallbackInfo ci) {
        Text messageText = packet.content();
        String message = messageText.getString();

        // Only global messages
        if (!message.startsWith("\uDBC0\uDC98\uF801")) return;

        String currChatLang = MineboxAdditionsClient.INSTANCE.modState.getChatLang();

        ParsedMessage result = Utils.extractPlayerNameAndMessage(message);
        if (result != null && currChatLang != null) {
            if(MinecraftClient.getInstance().getLanguageManager().getLanguage().contains(currChatLang)) {
                MineboxAdditionsClient.INSTANCE.modState.getSocket().emit("C2SChatMessage", currChatLang, result.playerName, result.message);
            }
        }

    }
}