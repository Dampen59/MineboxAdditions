package io.dampen59.mineboxadditions.mixins;

import net.minecraft.client.texture.GlTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlTexture.class)
public interface BrowserTextureMixin {
    @Accessor("glId")
    @Mutable
    void setId(int id);
}