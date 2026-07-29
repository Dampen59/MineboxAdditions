package io.dampen59.mineboxadditions.features.fishingshoal;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.render.categories.FishingShoals;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.ImageUtils;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.Display;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;

import static io.dampen59.mineboxadditions.utils.ImageUtils.textureExists;

public class FishingShoalDisplay {
    private static List<FishingShoal.Item> shoalItems = new ArrayList<>();

    public static void init() { }

    public static void loadFromApi(List<FishingShoal.Item> items) {
        for (FishingShoal.Item item : items) {
            if (item.getTexture() == null) {
                MineboxAdditions.LOGGER.warn("Fish {} has null texture data", item.getName());
                continue;
            }
            String textureName = "textures/fish/" + item.getName() + ".png";
            Identifier resource = ImageUtils.createTextureFromBase64(item.getTexture(), textureName);
            if (resource != null) item.setResource(resource);
        }
        shoalItems = items;
    }

    public static void handle(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        AABB box = client.player.getBoundingBox()
                .inflate(FishingShoals.renderRadius);
        Map<Entity, String> shoals = new HashMap<>();

        for (Entity entity : client.level.getEntities(client.player, box,
                e -> e instanceof Display.TextDisplay)) {
            String key = getCachedEntityTextKey(entity);
            if (key != null && key.startsWith("mbx.harvestables.shoal")) {
                shoals.put(entity, key);
            }
        }

        if (!shoals.isEmpty()) {
            shoals.forEach((entity, key) -> render(entity, key, context));
        }
    }

    private static void render(Entity entity, String translationKey, LevelRenderContext context) {
        if (!isBillboardEntity(entity)) return;
        if (!translationKey.contains("shoal")) return;
        String shoalName = translationKey.split("harvestables\\.")[1].split("\\.name")[0];

        PoseStack matrices = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        if (matrices == null || collector == null) return;

        matrices.pushPose();
        Vec3 entityPos = entity.position().subtract(context.levelState().cameraRenderState.pos);
        matrices.translate(entityPos.x, entityPos.y - 0.5, entityPos.z);

        List<Identifier> textures = getTexture(shoalName, Minecraft.getInstance().level);
        var localPlayer = Minecraft.getInstance().player;
        float yaw = (localPlayer != null) ? localPlayer.getYRot() : 0f;
        float pitch = (localPlayer != null) ? localPlayer.getXRot() : 0f;

        float textureSize = 0.5f;
        float spacing = 0.2f;
        int count = textures.size();
        float totalWidth = (textureSize * count) + (spacing * (count - 1));
        float startOffset = -totalWidth / 2 + textureSize / 2;

        for (int i = 0; i < textures.size(); i++) {
            Identifier texture = textures.get(i);

            matrices.pushPose();
            float xOffset = startOffset + i * (textureSize + spacing);

            matrices.mulPose(new Quaternionf()
                    .rotationYXZ((float) Math.toRadians(-yaw), (float) Math.toRadians(pitch), 0));
            matrices.translate(xOffset, 0, 0);

            float half = textureSize / 2;

            collector.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(texture), (pose, buffer) -> {
                Matrix4f matrix = pose.pose();
                buffer.addVertex(matrix, -half, -half, 0).setColor(255, 255, 255, 255).setUv(1, 1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
                buffer.addVertex(matrix, -half, half, 0).setColor(255, 255, 255, 255).setUv(1, 0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
                buffer.addVertex(matrix, half, half, 0).setColor(255, 255, 255, 255).setUv(0, 0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
                buffer.addVertex(matrix, half, -half, 0).setColor(255, 255, 255, 255).setUv(0, 1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0, 0, 1);
            });

            matrices.popPose();
        }
        matrices.popPose();
    }

    private static boolean isBillboardEntity(Entity entity) {
        if (entity instanceof ArmorStand stand) {
            boolean hasEquipment =
                    !stand.getItemBySlot(EquipmentSlot.HEAD).isEmpty()  ||
                            !stand.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ||
                            !stand.getItemBySlot(EquipmentSlot.LEGS).isEmpty()  ||
                            !stand.getItemBySlot(EquipmentSlot.FEET).isEmpty();

            if (!hasEquipment && (stand.isNoGravity() || stand.isInvisible())) {
                return true;
            }
        }

        if (entity instanceof Display.TextDisplay) {
            return true;
        }

        boolean isStationary = entity.isNoGravity() || entity.getDeltaMovement().lengthSqr() < 0.0001;
        boolean hasText = getEntityText(entity) != null;
        boolean isSpecial = entity.getType() == EntityTypes.INTERACTION
                || entity.getType() == EntityTypes.AREA_EFFECT_CLOUD
                || entity.getType() == EntityTypes.MARKER;

        return (isStationary && hasText) || isSpecial;
    }

    private static Component getEntityText(Entity entity) {
        if (entity.hasCustomName()) {
            return entity.getCustomName();
        }
        if (entity instanceof Display.TextDisplay textDisplay) {
            Component t = textDisplay.getText();
            if (t != null && !t.getString().isEmpty()) {
                return t;
            }
        }
        return null;
    }

    private static List<Identifier> getTexture(String shoal, Level world) {
        State state = MineboxAdditions.INSTANCE.state;
        List<Identifier> textures = new ArrayList<>();
        boolean isRaining = world.isRaining();
        boolean isStorming = world.isThundering();
        boolean isFullMoon = state.getCurrentMoonPhase() == 0;
        long currentWorldTicks = world.clockManager().getTotalTicks(world.registryAccess().getOrThrow(WorldClocks.OVERWORLD)) % 24000;

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
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
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
        String uuid = entity.getUUID().toString();
        State state = MineboxAdditions.INSTANCE.state;
        if (state.hasEntityTextCached(uuid)) {
            return state.getCachedEntityText(uuid);
        }
        String result = getEntityTextKeyUncached(entity);
        state.cacheEntityText(uuid, result);
        return result;
    }

    private static String getEntityTextKeyUncached(Entity entity) {
        Component t = entity.hasCustomName() ? entity.getCustomName() : null;
        if (t == null && entity instanceof Display.TextDisplay td) {
            t = td.getText();
        }
        if (t == null) return null;
        return extractShoalTranslationKey(t);
    }

    private static String extractShoalTranslationKey(Component text) {
        if (text == null) return null;

        if (text.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            if (key != null && key.startsWith("mbx.harvestables.shoal")) {
                return key;
            }
            for (Object arg : tc.getArgs()) {
                if (arg instanceof Component tArg) {
                    String found = extractShoalTranslationKey(tArg);
                    if (found != null) return found;
                }
            }
        }

        for (Component sibling : text.getSiblings()) {
            String found = extractShoalTranslationKey(sibling);
            if (found != null) return found;
        }

        return null;
    }
}