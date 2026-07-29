package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.config.render.RenderConfig;
import io.dampen59.mineboxadditions.features.ContainerSearch;
import io.dampen59.mineboxadditions.features.item.ItemRarity;
import io.dampen59.mineboxadditions.features.item.MuseumIndicator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow protected int topPos;
    @Shadow protected int leftPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mbx$initSearchBar(CallbackInfo ci) {
        if (!RenderConfig.containerSearch) return;
        EditBox searchBox = new EditBox(
                this.font,
                leftPos,
                topPos + imageHeight + 8,
                imageWidth,
                18,
                Component.empty()
        );
        searchBox.setHint(Component.literal("Search..."));
        searchBox.setMaxLength(50);
        searchBox.setValue(ContainerSearch.getQuery());
        searchBox.setResponder(ContainerSearch::setQuery);
        this.addRenderableWidget(searchBox);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mbx$consumeKeyWhenSearchFocused(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!(this.getFocused() instanceof EditBox editBox) || !editBox.isFocused()) return;
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) return;
        editBox.keyPressed(event);
        cir.setReturnValue(true);
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void mbx$renderSlotBackground(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ItemRarity.renderSlot(context, slot, slot.x, slot.y);
        MuseumIndicator.renderSlot(context, slot, slot.x, slot.y);
    }

    @Inject(method = "extractSlot", at = @At("TAIL"))
    private void mbx$renderSlotSearchOverlay(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ContainerSearch.renderSlotOverlay(context, slot);
    }
}
