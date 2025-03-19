package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.minebox.MineboxFishingShoal;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import java.util.ArrayList;
import java.util.List;

import static io.dampen59.mineboxadditions.utils.ImageUtils.textureExists;

public class WorldRendererEvent {

    public WorldRendererEvent() {
        WorldRenderEvents.AFTER_ENTITIES.register(WorldRendererEvent::onRender);
    }

    public static void onRender(WorldRenderContext context) {
        State state = MineboxAdditionsClient.INSTANCE.modState;
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if(!state.getConnectedToMinebox()) return;
        if(!config.fishingSettings.showFishDrops) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if(mc.player == null) return;
        if(world == null) return;
        String worldID = world.getRegistryKey().getValue().toString();
        if(worldID.equals("minecraft:overworld") || worldID.equals("minecraft:island_tropical")) {
            Box searchBox = mc.player.getBoundingBox().expand(config.fishingSettings.fishDropRadius);
            List<Entity> entities = mc.world.getOtherEntities(mc.player, searchBox)
                    .stream()
                    .filter(WorldRendererEvent::isFishingShoal)
                    .toList();
            if(entities.isEmpty()) return;
            entities.forEach((entity) -> doRender(entity, context, state));
        }
    }

    private static List<Identifier> getTexture(String shoal, World world, State state) {
        List<MineboxFishingShoal.FishingShoalFish> fishables = state.getMbxFishables();
        List<Identifier> textures = new ArrayList<>();
        boolean isRaining = world.isRaining();
        boolean isStorming = world.isThundering();
        boolean isFullMoon = state.getCurrentMoonPhase() == 0;
        long currentWorldTicks = world.getTimeOfDay() % 24000;

        fishables.forEach((fish) -> {
            if (fish.getShoal().equals(shoal)) {
                // First check time range - if outside time range, don't render regardless of other conditions
                boolean timeInRange = isTimeInRange(fish, currentWorldTicks);

                if (!timeInRange) {
                    return; // Skip this fish if not in time range
                }

                // Weather conditions check
                MineboxFishingShoal.FishingShoalConditions conditions = fish.getConditions();

                // If any weather condition is specifically required (true), check if it matches
                boolean weatherConditionRequired = false;
                boolean weatherConditionMet = true;

                if (Boolean.TRUE.equals(conditions.getRain())) {
                    weatherConditionRequired = true;
                    if (!isRaining) weatherConditionMet = false;
                }

                if (Boolean.TRUE.equals(conditions.getStorm())) {
                    weatherConditionRequired = true;
                    if (!isStorming) weatherConditionMet = false;
                }

                if (Boolean.TRUE.equals(conditions.getFullMoon())) {
                    weatherConditionRequired = true;
                    if (!isFullMoon) weatherConditionMet = false;
                }

                // Add texture if:
                // 1. No specific weather is required (all conditions are false), OR
                // 2. A specific weather is required and the condition is met
                if (!weatherConditionRequired || weatherConditionMet) {
                    // Check if texture is already loaded
                    // If not, load it from base64 and register it
                    // If texture is null, skip this fish
                    TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                    Identifier resourceID = fish.getResource();
                    if(textureExists(textureManager, resourceID)) {
                        textures.add(resourceID);
                    }
                }
            }
        });
        return textures;
    }

    private static boolean isTimeInRange(MineboxFishingShoal.FishingShoalFish fish, long currentWorldTicks) {
        boolean timeInRange;
        int minTime = fish.getTimeRange().get(0);
        int maxTime = fish.getTimeRange().get(1);

        // Handle time ranges that don't cross midnight
        if (minTime <= maxTime) {
            timeInRange = currentWorldTicks >= minTime && currentWorldTicks <= maxTime;
        }
        // Handle time ranges that cross midnight (e.g., [18000, 6000])
        else {
            timeInRange = currentWorldTicks >= minTime || currentWorldTicks <= maxTime;
        }
        return timeInRange;
    }

    private static void doRender(Entity entity, WorldRenderContext context, State state) {
        if (WorldRendererEvent.isBillboardEntity(entity)) {
            String billboardTextTranslationKey = getEntityTextKey(entity);
            if(billboardTextTranslationKey == null) return;
            if(!billboardTextTranslationKey.contains("shoal")) return;
            String shoalName = billboardTextTranslationKey.split("harvestables.")[1].split(".name")[0];

            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider vertexConsumers = context.consumers();
            if (matrices == null || vertexConsumers == null) return;

            matrices.push();
            Vec3d entityPos = entity.getPos().subtract(context.camera().getPos());
            matrices.translate(entityPos.x, entityPos.y - 0.5, entityPos.z);

            List<Identifier> textures = getTexture(shoalName, context.world(), state);

            // Get camera orientation
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            float yaw = camera.getYaw();
            float pitch = camera.getPitch();

            // Layout parameters
            float textureSize = 0.5f;
            float spacing = 0.2f;
            int count = textures.size();
            float totalWidth = (textureSize * count) + (spacing * (count - 1));
            float startOffset = -totalWidth / 2 + textureSize/2;

            for (int i = 0; i < textures.size(); i++) {
                Identifier texture = textures.get(i);
                VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));

                matrices.push();

                // Calculate position in row
                float xOffset = startOffset + i * (textureSize + spacing);

                // Apply camera-based rotation and position
                matrices.multiply(new Quaternionf()
                        .rotationYXZ(
                                (float) Math.toRadians(-yaw),
                                (float) Math.toRadians(pitch),
                                0
                        )
                );
                matrices.translate(xOffset, 0, 0);

                Matrix4f matrix = matrices.peek().getPositionMatrix();
                float half = textureSize / 2;

                // Billboard quad
                buffer.vertex(matrix, -half, -half, 0)
                        .color(255, 255, 255, 255)
                        .texture(0, 1)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(15728880)
                        .normal(0, 0, 1);

                buffer.vertex(matrix, -half, half, 0)
                        .color(255, 255, 255, 255)
                        .texture(0, 0)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(15728880)
                        .normal(0, 0, 1);

                buffer.vertex(matrix, half, half, 0)
                        .color(255, 255, 255, 255)
                        .texture(1, 0)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(15728880)
                        .normal(0, 0, 1);

                buffer.vertex(matrix, half, -half, 0)
                        .color(255, 255, 255, 255)
                        .texture(1, 1)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(15728880)
                        .normal(0, 0, 1);

                matrices.pop();
            }
            matrices.pop();
        }
    }

    private static boolean isBillboardEntity(Entity entity) {
        // ArmorStand specific checks
        if (entity instanceof ArmorStandEntity stand) {
            // Check if armor stand has any equipment or is purely for display
            boolean hasEquipment = false;
            for (var stack : stand.getArmorItems()) {
                if (!stack.isEmpty()) {
                    hasEquipment = true;
                    break;
                }
            }
            if (!hasEquipment && (stand.hasNoGravity() || stand.isInvisible())) {
                return true;
            }
        }

        // Text Display Entity
        if (entity instanceof DisplayEntity.TextDisplayEntity) {
            return true;
        }

        // Check for common billboard characteristics
        boolean isStationary = entity.hasNoGravity() ||
                entity.getVelocity().lengthSquared() < 0.0001;
        boolean hasText = getEntityText(entity) != null;
        boolean isSpecialType = entity.getType() == EntityType.INTERACTION ||
                entity.getType() == EntityType.AREA_EFFECT_CLOUD ||
                entity.getType() == EntityType.MARKER;

        return (isStationary && hasText) || isSpecialType;
    }

    private static boolean isFishingShoal(Entity entity) {
        String name = getEntityTextKey(entity);
        if (name == null) return false;
        return name.startsWith("mbx.harvestables.shoal");
    }

    public static Text getEntityText(Entity entity) {
        if (entity.hasCustomName()) {
            return entity.getCustomName();
        }

        NbtCompound nbt = new NbtCompound();
        entity.writeNbt(nbt);

        if (nbt.contains("text")) {
            String textCompound = nbt.getString("text");
            if (textCompound.contains("translate")) {
                String translateKey = textCompound.split("translate\":\"")[1].split("\"")[0];
                return Text.translatable(translateKey);
            }
        }

        return null;
    }

    private static String getEntityTextKey(Entity entity) {
        // Get the entity's NBT data
        NbtCompound nbt = new NbtCompound();
        entity.writeNbt(nbt);
        if (nbt.contains("text")) {
            String textCompound = nbt.getString("text");
            if (textCompound.contains("translate")) {
                return textCompound.split("translate\":\"")[1].split("\"")[0];
            } else {
                return null;
            }
        }
        return null;
    }
}
