package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.features.item.ItemRarity;
import io.dampen59.mineboxadditions.features.item.MuseumIndicator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void mbx$drawSlotsBefore(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        ItemRarity.render(context, screen);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void mbx$drawSlotsAfter(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object)this;
        MuseumIndicator.render(context, screen);
    }
}
