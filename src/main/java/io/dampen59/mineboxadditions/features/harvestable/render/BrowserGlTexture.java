package io.dampen59.mineboxadditions.features.harvestable.render;

import io.dampen59.mineboxadditions.mixins.BrowserTextureMixin;
import net.minecraft.client.texture.GlTexture;
import com.mojang.blaze3d.textures.TextureFormat;

public class BrowserGlTexture extends GlTexture {

    protected int width;
    protected int height;
    public BrowserGlTexture(int usage, String label, TextureFormat texFormat, int width, int height, int depthOrLayers, int mipLevels, int glId) {
        super(usage, label, texFormat, width, height, depthOrLayers, mipLevels, glId);
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth(int i) {
        return this.width >> i;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight(int i) {
        return this.height >> i;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getGlId() {
        return this.glId;
    }

    public void setGlId(int id) {
        ((BrowserTextureMixin) this).setId(id);
    }

}