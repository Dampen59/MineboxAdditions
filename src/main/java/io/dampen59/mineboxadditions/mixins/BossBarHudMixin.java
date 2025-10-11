package io.dampen59.mineboxadditions.mixins;

import io.dampen59.mineboxadditions.features.hud.Hud;
import io.dampen59.mineboxadditions.features.hud.HudManager;
import io.dampen59.mineboxadditions.features.hud.elements.TextElement;
import io.dampen59.mineboxadditions.features.hud.huds.IslandHud;
import io.dampen59.mineboxadditions.features.hud.huds.TimeHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private Map<UUID, ClientBossBar> bossBars;

//    private static final Pattern ISLAND_PATTERN = Pattern.compile("끫\\s*([^]+?)\\s*");
//    private static final Pattern TIME_PATTERN = Pattern.compile("끪\\s*([^]+?)\\s*");
//    private static final Pattern VOTE_PATTERN = Pattern.compile("\\s*([^]+?)\\s*");
//    private static final Map<Hud.Type, Pattern> HUD_PATTERNS = Map.of(
//            Hud.Type.ISLAND, ISLAND_PATTERN,
//            Hud.Type.TIME, TIME_PATTERN,
//            Hud.Type.VOTE, VOTE_PATTERN
//    );

    private Class<? extends Hud> getTypeByIcon(Text icon) {
        String iconText = icon.getString();
        if (iconText.contains("끫")) return IslandHud.class;
        if (iconText.contains("끪")) return TimeHud.class;
        return null;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, CallbackInfo ci) {
//        for (ClientBossBar bar : bossBars.values()) {
//            System.out.println(bar.getName());
//            String text = bar.getName().getString();
//
//            for (Map.Entry<Hud.Type, Pattern> entry : HUD_PATTERNS.entrySet()) {
//                Matcher matcher = entry.getValue().matcher(text);
//                if (!matcher.find()) continue;
//
//                Hud hud = HudManager.INSTANCE.getHud(entry.getKey());
//                if (hud == null) continue;
//
//                hud.getNamedElement("text", TextElement.class).setText(Text.of(matcher.group(1).trim()));
//            }
//        }
    }
}
