package io.dampen59.mineboxadditions.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    @Unique
    private ModConfig config = null;

    protected InventoryScreenMixin() {
        super(null);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderSlotBackgrounds(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.displaySettings.itemRaritySettings.displayItemsRarity) return;
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        int backgroundX = (this.width - screen.backgroundWidth) / 2;
        int backgroundY = (this.height - screen.backgroundHeight) / 2;

        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                if (!Utils.isMineboxItem(stack)) continue;
                Color rarityColor = RaritiesUtils.getItemRarityColorFromLore(stack);
                if (rarityColor != null) {
                    if (config.displaySettings.itemRaritySettings.displayMode == ModConfig.RaritiesDisplayMode.CIRCLE) {
                        drawCircularSlotBackground(context, slot.x + backgroundX, slot.y + backgroundY, rarityColor);
                    } else if (config.displaySettings.itemRaritySettings.displayMode == ModConfig.RaritiesDisplayMode.FILL) {
                        drawSlotBackground(context, slot.x + backgroundX, slot.y + backgroundY, rarityColor);
                    }
                }
            }
        }
    }

    @Unique
    private void drawCircularSlotBackground(DrawContext context, int x, int y, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int centerX = x + 8;
        int centerY = y + 8;
        int radius = 8;

        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            context.fill(centerX - dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color.getRGB());
        }
        RenderSystem.disableBlend();
    }

    @Unique
    private void drawSlotBackground(DrawContext context, int x, int y, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.fill(x, y, x + 16, y + 16, color.getRGB());
        RenderSystem.disableBlend();
    }
}
