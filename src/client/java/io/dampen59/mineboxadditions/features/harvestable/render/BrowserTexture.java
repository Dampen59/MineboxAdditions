package io.dampen59.mineboxadditions.features.harvestable.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;

public class BrowserTexture extends AbstractTexture {
    protected final BrowserGlTexture browserGlTexture;

    public BrowserTexture(int id, @NotNull String label) {
        this.browserGlTexture = new BrowserGlTexture(5, label, TextureFormat.RGBA8, 100, 100, 1, 1, id);
        this.browserGlTexture.setTextureFilter(FilterMode.NEAREST, false);
        this.glTexture = this.browserGlTexture;
        this.glTextureView = RenderSystem.getDevice().createTextureView(this.glTexture);
    }

    private void rebuildView() {
        this.glTextureView = RenderSystem.getDevice().createTextureView(this.glTexture);
    }

    public void setId(int id) {
        this.browserGlTexture.setGlId(id);
        rebuildView();
    }

    public void setWidth(int width) {
        this.browserGlTexture.setWidth(width);
        rebuildView();
    }

    public void setHeight(int height) {
        this.browserGlTexture.setHeight(height);
        rebuildView();
    }
}
