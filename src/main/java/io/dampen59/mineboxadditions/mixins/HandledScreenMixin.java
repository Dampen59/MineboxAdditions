package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.features.item.ItemRarity;
import io.dampen59.mineboxadditions.features.item.MuseumIndicator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void mbx$renderSlotBackground(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ItemRarity.renderSlot(context, slot, slot.x, slot.y);
        MuseumIndicator.renderSlot(context, slot, slot.x, slot.y);
    }
}
