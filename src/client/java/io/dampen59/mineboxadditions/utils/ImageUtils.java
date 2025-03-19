package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class ImageUtils {
    public static Identifier createTextureFromBufferedImage(BufferedImage image, String identifierName) {
        if (image == null) {
            MineboxAdditions.LOGGER.error("Cannot create texture from null image: {}", identifierName);
            return null;
        }

        try {
            // Convert BufferedImage to NativeImage
            NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    nativeImage.setColorArgb(x, y, argb);
                }
            }

            // Create the identifier
            Identifier identifier = Identifier.of("mineboxadditions", identifierName);

            // Register the texture on the main thread
            MinecraftClient.getInstance().execute(() -> {
                try {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(
                            identifier, new NativeImageBackedTexture(nativeImage));
                    MineboxAdditions.LOGGER.info("Texture registered on render thread: {}", identifierName);
                } catch (Exception e) {
                    MineboxAdditions.LOGGER.error("Error registering texture on render thread: {}\n{}", e.getMessage(), e.getStackTrace());
                }
            });

            return identifier;
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error("Error creating texture: {}\n{}", e.getMessage(), e.getStackTrace());
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

    public static String encodeImageToBase64(String filePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            MineboxAdditions.LOGGER.error(e.toString());
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

            // Log a small sample of the base64 for debugging (first 50 chars)
            String sampleBase64 = base64Image.length() > 50 ?
                    base64Image.substring(0, 50) + "..." : base64Image;
            MineboxAdditions.LOGGER.info("Decoding base64 image (sample): " + sampleBase64);

            // Decode the Base64 string
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Image);
                MineboxAdditions.LOGGER.info("Successfully decoded " + imageBytes.length + " bytes");
            } catch (IllegalArgumentException e) {
                MineboxAdditions.LOGGER.error("Failed to decode base64: " + e.getMessage());
                return null;
            }

            if (imageBytes.length == 0) {
                MineboxAdditions.LOGGER.error("Decoded base64 resulted in empty byte array");
                return null;
            }

            // Try to determine image format from the bytes
            String format = determineImageFormat(imageBytes);
            MineboxAdditions.LOGGER.info("Detected image format: " + (format != null ? format : "unknown"));

            // Convert the byte array to a BufferedImage
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                MineboxAdditions.LOGGER.error("ImageIO could not read the decoded data as an image");
                return null;
            }

            MineboxAdditions.LOGGER.info("Successfully created image: " + image.getWidth() + "x" + image.getHeight());
            return image;
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error("Error decoding base64 to image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to try to determine image format from byte array
    private static String determineImageFormat(byte[] imageData) {
        if (imageData.length < 12) return null;

        // Check for PNG signature
        if (imageData[0] == (byte)0x89 && imageData[1] == (byte)0x50 &&
                imageData[2] == (byte)0x4E && imageData[3] == (byte)0x47) {
            return "PNG";
        }

        // Check for JPEG signature (there are multiple possible signatures)
        if (imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8 &&
                imageData[2] == (byte)0xFF) {
            return "JPEG";
        }

        // Check for GIF signature
        if (imageData[0] == (byte)0x47 && imageData[1] == (byte)0x49 &&
                imageData[2] == (byte)0x46 && imageData[3] == (byte)0x38) {
            return "GIF";
        }

        return null; // Unknown format
    }


    public static BufferedImage getTextureAsBufferedImage(ResourceManager resourceManager, String texturePath) {
        // Create the Identifier for the texture
        Identifier textureId = Identifier.of("mineboxaddons", texturePath);
        if(resourceManager.getResource(textureId).isEmpty()) {
            MineboxAdditions.LOGGER.error("Resource not found: {}", textureId);
            return null;
        }
        try (InputStream inputStream = resourceManager.getResource(textureId).get().getInputStream()) {
            // Read the file into a BufferedImage
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            MineboxAdditions.LOGGER.error(e.toString());
            return null; // Handle the error appropriately
        }
    }

    public static boolean textureExists(TextureManager textureManager, Identifier textureId) {
        try {
            AbstractTexture texture = textureManager.getTexture(textureId);

            // If the texture is null, or it's the missing texture, it doesn't exist
            if (texture == null || texture == MissingSprite.getMissingSpriteTexture()) {
                return false;
            }

            // Check if the texture is valid by accessing its GL ID
            // This will throw an exception if the texture is not properly loaded
            int glId = texture.getGlId();
            return glId != 0;
        } catch (Exception e) {
            MineboxAdditions.LOGGER.warn("Error checking if texture exists: {}", textureId, e);
            return false; // Texture doesn't exist or couldn't be accessed
        }
    }

}