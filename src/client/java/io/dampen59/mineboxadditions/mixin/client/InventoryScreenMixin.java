package io.dampen59.mineboxadditions.mixin.client;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
            drawBorder(context, slot.x, slot.y, 16, 1, hueOffset);
        }
    }

    private static void drawBorder(DrawContext ctx, int x, int y, int size, int thickness, float hueOffset) {

        for (int i = 0; i < size; i++) {

            float f = i / (float) size;

            int topRGB    = Color.HSBtoRGB((hueOffset + 0.00f + f) % 1f, 1.0f, 1.0f);
            int rightRGB  = Color.HSBtoRGB((hueOffset + 0.25f + f) % 1f, 1.0f, 1.0f);
            int bottomRGB = Color.HSBtoRGB((hueOffset + 0.50f + f) % 1f, 1.0f, 1.0f);
            int leftRGB   = Color.HSBtoRGB((hueOffset + 0.75f + f) % 1f, 1.0f, 1.0f);

            int topARGB     = 0xFF000000 | (topRGB & 0x00FFFFFF);
            int rightARGB   = 0xFF000000 | (rightRGB & 0x00FFFFFF);
            int bottomARGB  = 0xFF000000 | (bottomRGB & 0x00FFFFFF);
            int leftARGB    = 0xFF000000 | (leftRGB & 0x00FFFFFF);

            ctx.fill(x + i, y, x + i + 1, y + thickness, topARGB);
            ctx.fill(x + i, y + size - thickness, x + i + 1, y + size, bottomARGB);
            ctx.fill(x, y + i, x + thickness, y + i + 1, leftARGB);
            ctx.fill(x + size - thickness, y + i, x + size, y + i + 1, rightARGB);
        }
    }
}