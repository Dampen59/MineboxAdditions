package io.dampen59.mineboxadditions.utils;

import com.mojang.blaze3d.platform.NativeImage;
import io.dampen59.mineboxadditions.MineboxAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;

public class ImageUtils {
    public static Identifier createTextureFromBufferedImage(BufferedImage image, String identifierName) {
        if (image == null) {
            MineboxAdditions.LOGGER.error("Cannot create texture from null image: {}", identifierName);
            return null;
        }

        try {
            NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    // BufferedImage.getRGB returns ARGB; NativeImage.setPixelABGR expects ABGR — swap R and B
                    int abgr = (argb & 0xFF00FF00)
                             | ((argb & 0x00FF0000) >> 16)
                             | ((argb & 0x000000FF) << 16);
                    nativeImage.setPixelABGR(x, y, abgr);
                }
            }

            Identifier id = Identifier.fromNamespaceAndPath("mineboxadditions", identifierName);

            Runnable register = () -> {
                try {
                    DynamicTexture tex = new DynamicTexture(id::toString, nativeImage);
                    Minecraft.getInstance().getTextureManager().register(id, tex);
                    MineboxAdditions.LOGGER.info("Texture registered: {}", id);
                } catch (Exception e) {
                    MineboxAdditions.LOGGER.error("Error registering texture {}: ", id, e);
                }
            };

            if (Minecraft.getInstance().isSameThread()) {
                register.run();
            } else {
                Minecraft.getInstance().execute(register);
            }

            return id;
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error("Error creating texture {}: ", identifierName, e);
            return null;
        }
    }


    public static Identifier createTextureFromBase64(@NotNull String base64, String identifierName) {
        try {
            // Convert base64 string to BufferedImage
            BufferedImage image = decodeBase64ToImage(base64);
            return createTextureFromBufferedImage(image, identifierName);
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error(e.toString());
            MineboxAdditions.LOGGER.error(Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public static BufferedImage decodeBase64ToImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            MineboxAdditions.LOGGER.error("Base64 image string is null or empty");
            return null;
        }

        try {
            // Remove the data URL prefix if present
            if (base64Image.startsWith("data:image")) {
                MineboxAdditions.LOGGER.info("Detected data URL format, extracting base64 content");
                base64Image = base64Image.split(",")[1];
            }

            // Decode the Base64 string
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Image);
            } catch (IllegalArgumentException e) {
                MineboxAdditions.LOGGER.error("Failed to decode base64: {}", e.getMessage());
                return null;
            }

            if (imageBytes.length == 0) {
                MineboxAdditions.LOGGER.error("Decoded base64 resulted in empty byte array");
                return null;
            }

            // Convert the byte array to a BufferedImage
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                MineboxAdditions.LOGGER.error("ImageIO could not read the decoded data as an image");
                return null;
            }

            MineboxAdditions.LOGGER.info("Successfully created image: {}x{}", image.getWidth(), image.getHeight());
            return image;
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error("Error decoding base64 to image: {}\n{}", e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    public static boolean textureExists(TextureManager textureManager, Identifier id) {
        try {
            AbstractTexture tex = textureManager.getTexture(id);
            return tex != null;
        } catch (Exception e) {
            return false;
        }
    }
}