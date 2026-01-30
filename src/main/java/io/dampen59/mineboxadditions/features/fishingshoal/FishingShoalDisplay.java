package io.dampen59.mineboxadditions.features.fishingshoal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.FishingDrops;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import io.dampen59.mineboxadditions.utils.SocketManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;

import static io.dampen59.mineboxadditions.utils.ImageUtils.textureExists;

public class FishingShoalDisplay {
    private static List<FishingShoal.Item> shoalItems = new ArrayList<>();

    public static void init() {
        SocketManager.getSocket().on("S2CMineboxFishables", FishingShoalDisplay::update);
    }

    private static void update(Object[] args) {
        String jsonData = (String) args[0];

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<FishingShoal.Item> items = mapper.readValue(jsonData,
                    mapper.getTypeFactory().constructCollectionType(List.class,
                            FishingShoal.Item.class));

            for (FishingShoal.Item item : items) {
                if (item.getTexture() == null) {
                    MineboxAdditions.LOGGER.warn("Fish {} has null texture data", item.getName());
                    continue;
                }

                String textureName = "textures/fish/" + item.getName() + ".png";
                Identifier resource = ImageUtils.createTextureFromBase64(item.getTexture(), textureName);
                if (resource != null)
                    item.setResource(resource);
            }

            shoalItems = items;
        } catch (JsonProcessingException e) {
            MineboxAdditions.LOGGER.error("[SocketManager] Failed to load Minebox Fishables JSON: {}",
                    e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void handle(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Box box = client.player.getBoundingBox()
                .expand(FishingDrops.renderRadius);
        Map<Entity, String> shoals = new HashMap<>();

        for (Entity entity : client.world.getOtherEntities(client.player, box,
                e -> e instanceof DisplayEntity.TextDisplayEntity)) {
            String key = getCachedEntityTextKey(entity);
            if (key != null && key.startsWith("mbx.harvestables.shoal")) {
                shoals.put(entity, key);
            }
        }

        if (!shoals.isEmpty()) {
            shoals.forEach((entity, key) -> render(entity, key, context));
        }
    }

    private static void render(Entity entity, String translationKey, WorldRenderContext context) {
        if (!isBillboardEntity(entity)) return;
        if (!translationKey.contains("shoal")) return;
        String shoalName = translationKey.split("harvestables\\.")[1].split("\\.name")[0];

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vertexConsumers = context.consumers();
        if (matrices == null || vertexConsumers == null) return;

        matrices.push();
        Vec3d entityPos = entity.getPos().subtract(context.camera().getPos());
        matrices.translate(entityPos.x, entityPos.y - 0.5, entityPos.z);

        List<Identifier> textures = getTexture(shoalName, context.world());
        var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        float textureSize = 0.5f;
        float spacing = 0.2f;
        int count = textures.size();
        float totalWidth = (textureSize * count) + (spacing * (count - 1));
        float startOffset = -totalWidth / 2 + textureSize / 2;

        for (int i = 0; i < textures.size(); i++) {
            Identifier texture = textures.get(i);
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));

            matrices.push();
            float xOffset = startOffset + i * (textureSize + spacing);

            matrices.multiply(new Quaternionf()
                    .rotationYXZ((float) Math.toRadians(-yaw), (float) Math.toRadians(pitch), 0));
            matrices.translate(xOffset, 0, 0);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float half = textureSize / 2;

            buffer.vertex(matrix, -half, -half, 0).color(255, 255, 255, 255).texture(1, 1)
                    .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
            buffer.vertex(matrix, -half, half, 0).color(255, 255, 255, 255).texture(1, 0)
                    .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
            buffer.vertex(matrix, half, half, 0).color(255, 255, 255, 255).texture(0, 0)
                    .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);
            buffer.vertex(matrix, half, -half, 0).color(255, 255, 255, 255).texture(0, 1)
                    .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(0, 0, 1);

            matrices.pop();
        }
        matrices.pop();
    }

    private static boolean isBillboardEntity(Entity entity) {
        if (entity instanceof ArmorStandEntity stand) {
            boolean hasEquipment =
                    !stand.getEquippedStack(EquipmentSlot.HEAD).isEmpty()  ||
                            !stand.getEquippedStack(EquipmentSlot.CHEST).isEmpty() ||
                            !stand.getEquippedStack(EquipmentSlot.LEGS).isEmpty()  ||
                            !stand.getEquippedStack(EquipmentSlot.FEET).isEmpty();

            if (!hasEquipment && (stand.hasNoGravity() || stand.isInvisible())) {
                return true;
            }
        }

        if (entity instanceof DisplayEntity.TextDisplayEntity) {
            return true;
        }

        boolean isStationary = entity.hasNoGravity() || entity.getVelocity().lengthSquared() < 0.0001;
        boolean hasText = getEntityText(entity) != null;
        boolean isSpecial = entity.getType() == EntityType.INTERACTION
                || entity.getType() == EntityType.AREA_EFFECT_CLOUD
                || entity.getType() == EntityType.MARKER;

        return (isStationary && hasText) || isSpecial;
    }

    private static Text getEntityText(Entity entity) {
        if (entity.hasCustomName()) {
            return entity.getCustomName();
        }
        if (entity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
            Text t = textDisplay.getText();
            if (t != null && !t.getString().isEmpty()) {
                return t;
            }
        }
        return null;
    }

    private static List<Identifier> getTexture(String shoal, World world) {
        State state = MineboxAdditions.INSTANCE.state;
        List<Identifier> textures = new ArrayList<>();
        boolean isRaining = world.isRaining();
        boolean isStorming = world.isThundering();
        boolean isFullMoon = state.getCurrentMoonPhase() == 0;
        long currentWorldTicks = world.getTimeOfDay() % 24000;

        for (FishingShoal.Item item : shoalItems) {
            if (!item.getShoal().equals(shoal)) continue;
            if (!isTimeInRange(item, currentWorldTicks)) continue;

            var conditions = item.getConditions();
            boolean weatherRequired = Boolean.TRUE.equals(conditions.getRain())
                    || Boolean.TRUE.equals(conditions.getStorm()) || Boolean.TRUE.equals(conditions.getFullMoon());
            boolean weatherMet = (!Boolean.TRUE.equals(conditions.getRain()) || isRaining) &&
                    (!Boolean.TRUE.equals(conditions.getStorm()) || isStorming) &&
                    (!Boolean.TRUE.equals(conditions.getFullMoon()) || isFullMoon);

            if (!weatherRequired || weatherMet) {
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                Identifier resourceID = item.getResource();
                if (textureExists(textureManager, resourceID)) {
                    textures.add(resourceID);
                }
            }
        }
        return textures;
    }

    private static boolean isTimeInRange(FishingShoal.Item item, long currentWorldTicks) {
        int minTime = item.getTimeRange().get(0);
        int maxTime = item.getTimeRange().get(1);
        return minTime <= maxTime
                ? currentWorldTicks >= minTime && currentWorldTicks <= maxTime
                : currentWorldTicks >= minTime || currentWorldTicks <= maxTime;
    }

    private static String getCachedEntityTextKey(Entity entity) {
        String uuid = entity.getUuid().toString();
        State state = MineboxAdditions.INSTANCE.state;
        if (state.hasEntityTextCached(uuid)) {
            return state.getCachedEntityText(uuid);
        }
        String result = getEntityTextKeyUncached(entity);
        state.cacheEntityText(uuid, result);
        return result;
    }

    private static String getEntityTextKeyUncached(Entity entity) {
        Text t = entity.hasCustomName() ? entity.getCustomName() : null;
        if (t == null && entity instanceof DisplayEntity.TextDisplayEntity td) {
            t = td.getText();
        }
        if (t == null) return null;
        return extractShoalTranslationKey(t);
    }

    private static String extractShoalTranslationKey(Text text) {
        if (text == null) return null;

        if (text.getContent() instanceof TranslatableTextContent tc) {
            String key = tc.getKey();
            if (key != null && key.startsWith("mbx.harvestables.shoal")) {
                return key;
            }
            for (Object arg : tc.getArgs()) {
                if (arg instanceof Text tArg) {
                    String found = extractShoalTranslationKey(tArg);
                    if (found != null) return found;
                }
            }
        }

        for (Text sibling : text.getSiblings()) {
            String found = extractShoalTranslationKey(sibling);
            if (found != null) return found;
        }

        return null;
    }
}