package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void onCharTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if(!config.bankMacro) return;

        if (isBankingScreen(currentScreen) && (chr == 'k' || chr == 'K')) {
            TextFieldWidget self = (TextFieldWidget) (Object) this;
            int cursorPosition = self.getCursor();
            String newText = new StringBuilder(self.getText())
                    .insert(cursorPosition, "000")
                    .toString();
            self.setText(newText);
            self.setCursor(cursorPosition + 3, false);
            cir.setReturnValue(true); // Prevent default handling
        }

        if (isBankingScreen(currentScreen) && (chr == 'm' || chr == 'M')) {
            TextFieldWidget self = (TextFieldWidget) (Object) this;
            int cursorPosition = self.getCursor();
            String newText = new StringBuilder(self.getText())
                    .insert(cursorPosition, "000000")
                    .toString();
            self.setText(newText);
            self.setCursor(cursorPosition + 6, false);
            cir.setReturnValue(true); // Prevent default handling
        }

        if (isBankingScreen(currentScreen) && (chr == 'b' || chr == 'B')) {
            TextFieldWidget self = (TextFieldWidget) (Object) this;
            int cursorPosition = self.getCursor();
            String newText = new StringBuilder(self.getText())
                    .insert(cursorPosition, "000000000")
                    .toString();
            self.setText(newText);
            self.setCursor(cursorPosition + 9, false);
            cir.setReturnValue(true); // Prevent default handling
        }
    }

    @Unique
    private boolean isBankingScreen(Screen screen) {
        if (screen instanceof HandledScreen<?> handledScreen) {
            Text title = handledScreen.getTitle();
            String titleString = title.getString();
            String withdrawText = Text.translatable("text.bank.withdraw").getString();
            String depositText = Text.translatable("text.bank.deposit").getString();
            return titleString.contains(withdrawText) || titleString.contains(depositText);
        }
        return false;
    }
}