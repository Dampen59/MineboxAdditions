package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.awt.Color;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    protected InventoryScreenMixin() { super(null); }

    @Inject(
            method = "renderMain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlots(Lnet/minecraft/client/gui/DrawContext;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void renderRarityBackgroundsUnderItems(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!cfg.displaySettings.itemRaritySettings.displayItemsRarity) return;

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        for (Slot slot : screen.getScreenHandler().slots) {
            if (!slot.isEnabled() || !slot.hasStack()) continue;

            ItemStack stack = slot.getStack();
            if (!Utils.isMineboxItem(stack)) continue;

            Color rarity = RaritiesUtils.getItemRarityColorFromLore(stack);
            if (rarity == null) continue;

            int argb = rarity.getRGB();

            if (cfg.displaySettings.itemRaritySettings.displayMode == ModConfig.RaritiesDisplayMode.CIRCLE) {
                drawCircle(context, slot.x, slot.y, argb);
            } else if (cfg.displaySettings.itemRaritySettings.displayMode == ModConfig.RaritiesDisplayMode.FILL) {
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, argb);
            }
        }
    }

    private static void drawCircle(DrawContext ctx, int x, int y, int argb) {
        int cx = x + 8, cy = y + 8, r = 8;
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, argb);
        }
    }

    @Inject(
            method = "renderMain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlots(Lnet/minecraft/client/gui/DrawContext;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderMissingMuseumItemsBorder(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!cfg.displaySettings.displayMuseumMissingItems) return;

        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;

        if (MineboxAdditionsClient.INSTANCE.modState == null) return;

        List<String> missing = MineboxAdditionsClient.INSTANCE.modState.getMissingMuseumItemIds();
        if (missing == null || missing.isEmpty()) return;

        final float hueOffset = ((System.currentTimeMillis() % 6000L) / 6000f);

        for (Slot slot : screen.getScreenHandler().slots) {
            if (!slot.isEnabled() || !slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            if (!Utils.isMineboxItem(stack)) continue;
            String id = Utils.getMineboxItemId(stack);
            if (id == null || id.isEmpty()) continue;
            if (!missing.contains(id)) continue;
            drawMuseumBorder(context, slot.x, slot.y, hueOffset);
        }
    }

    private static void drawMuseumBorder(DrawContext ctx, int x, int y, float hueOffset) {
        int top    = hsvToArgb((hueOffset + 0.00f) % 1f);
        int right  = hsvToArgb((hueOffset + 0.25f) % 1f);
        int bottom = hsvToArgb((hueOffset + 0.50f) % 1f);
        int left   = hsvToArgb((hueOffset + 0.75f) % 1f);

        ctx.fill(x, y, x + 16, y + 1, top);
        ctx.fill(x + 16 - 1, y, x + 16, y + 16, right);
        ctx.fill(x, y + 16 - 1, x + 16, y + 16, bottom);
        ctx.fill(x, y, x + 1, y + 16, left);
    }

    private static int hsvToArgb(float hue) {
        int rgb = java.awt.Color.HSBtoRGB(hue, (float) 1.0, (float) 1.0);
        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

}