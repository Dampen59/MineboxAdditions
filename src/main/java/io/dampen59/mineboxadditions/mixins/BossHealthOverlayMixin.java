package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.features.hud.BossBarScanner;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {

    @Redirect(
            method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;")
    )
    private Collection<LerpingBossEvent> mbx$hideHandledBars(Map<UUID, LerpingBossEvent> map) {
        List<LerpingBossEvent> visible = new ArrayList<>();
        for (LerpingBossEvent event : map.values()) {
            if (!BossBarScanner.isHandledBar(event.getName())) visible.add(event);
        }
        return visible;
    }
}
