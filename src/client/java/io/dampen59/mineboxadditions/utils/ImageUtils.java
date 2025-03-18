package io.dampen59.mineboxadditions.utils;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
        try {
            // Convert BufferedImage to NativeImage
            NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    nativeImage.setColorArgb(x, y, argb);
                }
            }
            // Register the texture
            Identifier identifier = Identifier.of("mineboxaddons", identifierName);
            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage));

            return identifier;
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
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
        try {
            // Remove the data URL prefix if present
            if (base64Image.startsWith("data:image")) {
                base64Image = base64Image.split(",")[1];
            }

            // Decode the Base64 string
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Convert the byte array to a BufferedImage
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);
        } catch (Exception e) {
            MineboxAdditions.LOGGER.error(e.toString());
            return null;
        }
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

    public static boolean textureExists(ResourceManager resourceManager, Identifier textureId) {
        try {
            Optional<Resource> resource = resourceManager.getResource(textureId);
            if (resource.isEmpty()) return false; // Resource doesn't exist
            resource.get().getInputStream().close(); // Attempt to open and close the stream
            return true; // If we got here without an exception, the texture exists
        } catch (Exception e) {
            return false; // Texture doesn't exist or couldn't be accessed
        }
    }
}