package io.dampen59.mineboxadditions.utils;

import com.mojang.blaze3d.textures.GpuTexture;
import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;

public class ImageUtils {
    public static Identifier createTextureFromBufferedImage(BufferedImage image, String identifierName) {
        if (image == null) {
            MineboxAdditionsClient.LOGGER.error("Cannot create texture from null image: {}", identifierName);
            return null;
        }

        try {
            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    nativeImage.setColorArgb(x, y, argb);
                }
            }

            Identifier id = Identifier.of("mineboxadditions", identifierName);

            Runnable register = () -> {
                try {
                    NativeImageBackedTexture tex = new NativeImageBackedTexture(id::toString, nativeImage);
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
                    MineboxAdditionsClient.LOGGER.info("Texture registered: {}", id);
                } catch (Exception e) {
                    MineboxAdditionsClient.LOGGER.error("Error registering texture {}: ", id, e);
                }
            };

            if (MinecraftClient.getInstance().isOnThread()) {
                register.run();
            } else {
                MinecraftClient.getInstance().execute(register);
            }

            return id;
        } catch (Exception e) {
            MineboxAdditionsClient.LOGGER.error("Error creating texture {}: ", identifierName, e);
            return null;
        }
    }


    public static Identifier createTextureFromBase64(@NotNull String base64, String identifierName) {
        try {
            // Convert base64 string to BufferedImage
            BufferedImage image = decodeBase64ToImage(base64);
            return createTextureFromBufferedImage(image, identifierName);
        } catch (Exception e) {
            MineboxAdditionsClient.LOGGER.error(e.toString());
            MineboxAdditionsClient.LOGGER.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static BufferedImage decodeBase64ToImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            MineboxAdditionsClient.LOGGER.error("Base64 image string is null or empty");
            return null;
        }

        try {
            // Remove the data URL prefix if present
            if (base64Image.startsWith("data:image")) {
                MineboxAdditionsClient.LOGGER.info("Detected data URL format, extracting base64 content");
                base64Image = base64Image.split(",")[1];
            }

            // Decode the Base64 string
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Image);
            } catch (IllegalArgumentException e) {
                MineboxAdditionsClient.LOGGER.error("Failed to decode base64: {}", e.getMessage());
                return null;
            }

            if (imageBytes.length == 0) {
                MineboxAdditionsClient.LOGGER.error("Decoded base64 resulted in empty byte array");
                return null;
            }

            // Convert the byte array to a BufferedImage
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                MineboxAdditionsClient.LOGGER.error("ImageIO could not read the decoded data as an image");
                return null;
            }

            MineboxAdditionsClient.LOGGER.info("Successfully created image: {}x{}", image.getWidth(), image.getHeight());
            return image;
        } catch (Exception e) {
            MineboxAdditionsClient.LOGGER.error("Error decoding base64 to image: {}\n{}", e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    public static boolean textureExists(TextureManager textureManager, Identifier id) {
        try {
            AbstractTexture tex = textureManager.getTexture(id);
            if (tex == null) return false;
            GpuTexture gpu = tex.getGlTexture();
            if (gpu == null || gpu.isClosed()) return false;
            if (gpu instanceof GlTexture gl) {
                int glId = gl.getGlId();
                return glId != 0;
            }
            return true;
        } catch (Exception e) {
            MineboxAdditionsClient.LOGGER.warn("Error checking if texture exists: {}", id, e);
            return false;
        }
    }
}