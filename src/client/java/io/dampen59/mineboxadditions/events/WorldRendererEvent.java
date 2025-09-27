package io.dampen59.mineboxadditions.events;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.dampen59.mineboxadditions.MineboxAdditionsClient;
import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.minebox.MineboxFishingShoal;
import io.dampen59.mineboxadditions.minebox.MineboxHarvestable;
import io.dampen59.mineboxadditions.state.State;
import io.dampen59.mineboxadditions.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

import static io.dampen59.mineboxadditions.utils.ImageUtils.textureExists;

public class WorldRendererEvent {

    private static final Identifier BEACON_BEAM_TEX = Identifier.of("textures/entity/beacon_beam.png");
    private static int tickCounter = 0;

    private static final int HIGHLIGHT_TICKS = 120;
    private static final float R = 1.0f, G = 1.0f, B = 1.0f, A = 0.85f;
    private static final int MAX_HIGHLIGHTS = 256;

    private static class HighlightEntry {
        final BlockPos pos;
        final long untilTick;
        HighlightEntry(BlockPos pos, long untilTick) {
            this.pos = pos.toImmutable();
            this.untilTick = untilTick;
        }
    }

    private static final List<HighlightEntry> ENTRIES = new ArrayList<>();

    public WorldRendererEvent() {
        WorldRenderEvents.AFTER_ENTITIES.register(WorldRendererEvent::onRender);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null)
                return;
            tickCounter++;
            if (tickCounter < 200)
                return;
            tickCounter = 0;

            Set<String> liveUuids = new HashSet<>();
            for (Entity entity : client.world.getEntities()) {
                liveUuids.add(entity.getUuid().toString());
            }
            MineboxAdditionsClient.INSTANCE.modState.cleanStaleEntityTextCache(liveUuids);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            if (!config.displaySettings.displayItemRange) return ActionResult.PASS;

            if (!world.isClient) return ActionResult.PASS;
            if (!(player instanceof ClientPlayerEntity)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            int itemSize = Utils.getItemSize(player.getStackInHand(Hand.MAIN_HAND));
            if (itemSize == 0) return ActionResult.PASS;

            Direction facing = player.getHorizontalFacing();
            BlockPos clicked = hit.getBlockPos();
            BlockPos target = clicked.offset(facing, itemSize);

            long now = world.getTime();
            if (ENTRIES.size() >= MAX_HIGHLIGHTS) {
                ENTRIES.removeFirst();
            }
            ENTRIES.add(new HighlightEntry(target, now + HIGHLIGHT_TICKS));
            return ActionResult.PASS;
        });

        WorldRenderEvents.LAST.register(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null || ENTRIES.isEmpty()) return;

            long now = mc.world.getTime();
            Vec3d camPos = context.camera().getPos();
            MatrixStack matrices = context.matrixStack();
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());

            Iterator<HighlightEntry> it = ENTRIES.iterator();
            while (it.hasNext()) {
                HighlightEntry e = it.next();
                if (now > e.untilTick) {
                    it.remove();
                    continue;
                }

                BlockState state = mc.world.getBlockState(e.pos);
                if (state.isAir()) continue;

                VoxelShape shape = state.getOutlineShape(mc.world, e.pos);
                if (shape.isEmpty()) continue;

                double ox = e.pos.getX() - camPos.x;
                double oy = e.pos.getY() - camPos.y;
                double oz = e.pos.getZ() - camPos.z;

                DebugRenderer.drawVoxelShapeOutlines(
                        matrices, buffer, shape,
                        ox, oy, oz,
                        R, G, B, A,
                        true
                );
            }
        });

    }

    public static void onRender(WorldRenderContext context) {
        State state = MineboxAdditionsClient.INSTANCE.modState;
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!state.isConnectedToMinebox())
            return;
        if (mc.player == null || mc.world == null)
            return;

        if (config.displaySettings.fishingSettings.showFishDrops) {
            Box searchBox = mc.player.getBoundingBox().expand(config.displaySettings.fishingSettings.fishDropRadius);
            Map<Entity, String> shoalEntities = new HashMap<>();

            for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox,
                    e -> e instanceof DisplayEntity.TextDisplayEntity)) {
                String key = getCachedEntityTextKey(entity);
                if (key != null && key.startsWith("mbx.harvestables.shoal")) {
                    shoalEntities.put(entity, key);
                }
            }

            if (!shoalEntities.isEmpty()) {
                shoalEntities.forEach((entity, key) -> doRenderShoalBillboard(entity, key, context, state));
            }
        }

        renderHarvestableBeams(context);
    }

    private static void renderHarvestableBeams(WorldRenderContext ctx) {
        var mc = MinecraftClient.getInstance();
        var world = ctx.world();
        if (world == null || mc.player == null)
            return;

        // get dim
        Identifier worldId = world.getRegistryKey().getValue();
        String islandKeyPath = worldId.getPath();

        var state = MineboxAdditionsClient.INSTANCE.modState;
        List<MineboxHarvestable> items = state.getMineboxHarvestables(islandKeyPath);
        if (items == null || items.isEmpty()) {
            items = state.getMineboxHarvestables(worldId.toString());
            if (items == null || items.isEmpty())
                return;
        }

        ModConfig cfg = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ModConfig.HarvestablesPrefs prefs = cfg.harvestablesPrefs.get(islandKeyPath);
        if (prefs == null)
            return;

        MatrixStack ms = ctx.matrixStack();
        VertexConsumerProvider prov = ctx.consumers();
        if (ms == null || prov == null)
            return;

        float tickDelta = ctx.tickCounter().getTickProgress(false);
        float time = (mc.world.getTime() + tickDelta);
        float scroll = (time / 40.0f) % 1.0f;

        // sft cull : as in vanilla mc render
        int rd = mc.options.getViewDistance().getValue();
        double maxDist = (rd * 16 + 64);
        double maxDistSq = maxDist * maxDist;

        Vec3d cam = ctx.camera().getPos();

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);

        for (MineboxHarvestable it : items) {
            String cat = it.getCategory() != null ? it.getCategory() : "misc";
            String name = it.getName() != null ? it.getName() : "unknown";

            boolean catOn = prefs.categoryEnabled.getOrDefault(cat, false);
            if (!catOn)
                continue;

            boolean itemOn = prefs.itemEnabled
                    .getOrDefault(cat, Collections.emptyMap())
                    .getOrDefault(name, false);
            if (!itemOn)
                continue;

            int rgb = prefs.itemColor
                    .getOrDefault(cat, Collections.emptyMap())
                    .getOrDefault(name, 0xFFFFFFFF);

            var c = it.getCoordinates();
            if (c == null || c.size() < 3)
                continue;
            int x = (int) Math.round(c.get(0));
            int y = (int) Math.round(c.get(1));
            int z = (int) Math.round(c.get(2));

            // dist cull
            double dx = x + 0.5 - cam.x;
            double dy = y - cam.y;
            double dz = z + 0.5 - cam.z;
            if (dx * dx + dy * dy + dz * dz > maxDistSq)
                continue;

            // cull near (prevent beam hiding hit particles on harvestables), asked by players
            Vec3d playerPos = mc.player.getPos();
            double ndx = x + 0.5 - playerPos.x;
            double ndy = y - playerPos.y;
            double ndz = z + 0.5 - playerPos.z;
            if ((ndx * ndx + ndy * ndy + ndz * ndz) < 16.0)
                continue;

            drawBeaconBeam(ms, prov, new BlockPos(x, y, z), 192, 0.35f, scroll, rgb);
        }

        ms.pop();
    }

    private static void drawBeaconBeam(MatrixStack ms, VertexConsumerProvider prov, BlockPos base,
                                       int height, float radiusIgnored, float vOffset, int rgb) {
        VertexConsumer vc = prov.getBuffer(RenderLayer.getBeaconBeam(BEACON_BEAM_TEX, true));
        Matrix4f m = ms.peek().getPositionMatrix();

        final float cx = base.getX() + 0.5f;
        final float cz = base.getZ() + 0.5f;
        final float y0 = base.getY();
        final float y1 = y0 + height;
        final float innerR = 0.20f;
        final float outerR = 0.25f;
        final float rot = (float) (vOffset * Math.PI * 2.0); // 0..2π
        final float sin = (float) Math.sin(rot);
        final float cos = (float) Math.cos(rot);
        final float u0 = 0f, u1 = 1f;
        final float v0 = vOffset;
        final float v1 = vOffset + (height / 32f);
        final int cr = (rgb >> 16) & 0xFF;
        final int cg = (rgb >>  8) & 0xFF;
        final int cb = (rgb      ) & 0xFF;
        float[][] inner = new float[4][2];
        float[][] outer = new float[4][2];

        fillRotatedSquare(inner, innerR, cos, sin);
        fillRotatedSquare(outer, outerR, cos, sin);

        // bem core
        final int aInner = 200;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            addBeamSide(vc, m,
                    cx + inner[i][0], cz + inner[i][1],   // bottom i  (x0,z0)
                    cx + inner[j][0], cz + inner[j][1],   // bottom j  (x1,z1)
                    y0, y1, u0, v0, u1, v1,
                    cr, cg, cb, aInner);
        }

        // out glow
        final int aOuter = 64;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            addBeamSide(vc, m,
                    cx + outer[i][0], cz + outer[i][1],
                    cx + outer[j][0], cz + outer[j][1],
                    y0, y1, u0, v0, u1, v1,
                    cr, cg, cb, aOuter);
        }
    }

    private static void fillRotatedSquare(float[][] out, float r, float cos, float sin) {
        float[][] base = { { r, -r }, { r,  r }, { -r,  r }, { -r, -r } };
        for (int i = 0; i < 4; i++) {
            float x = base[i][0], z = base[i][1];
            out[i][0] =  x * cos - z * sin;
            out[i][1] =  x * sin + z * cos;
        }
    }

    private static void addBeamSide(VertexConsumer vc, Matrix4f m,
                                    float x0, float z0, float x1, float z1,
                                    float y0, float y1,
                                    float u0, float v0, float u1, float v1,
                                    int r, int g, int b, int a) {
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE; // fullbright

        // bottom i
        vc.vertex(m, x0, y0, z0).color(r, g, b, a).texture(u0, v1)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        // bottom j
        vc.vertex(m, x1, y0, z1).color(r, g, b, a).texture(u1, v1)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        // top j
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).texture(u1, v0)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
        // top i
        vc.vertex(m, x0, y1, z0).color(r, g, b, a).texture(u0, v0)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0, 1, 0);
    }



    private static void doRenderShoalBillboard(Entity entity, String translationKey, WorldRenderContext context,
            State state) {
        if (!isBillboardEntity(entity))
            return;
        if (!translationKey.contains("shoal"))
            return;
        String shoalName = translationKey.split("harvestables\\.")[1].split("\\.name")[0];

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vertexConsumers = context.consumers();
        if (matrices == null || vertexConsumers == null)
            return;

        matrices.push();
        Vec3d entityPos = entity.getPos().subtract(context.camera().getPos());
        matrices.translate(entityPos.x, entityPos.y - 0.5, entityPos.z);

        List<Identifier> textures = getTexture(shoalName, context.world(), state);
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

    private static List<Identifier> getTexture(String shoal, World world, State state) {
        List<MineboxFishingShoal.FishingShoalFish> fishables = state.getMbxFishables();
        List<Identifier> textures = new ArrayList<>();
        boolean isRaining = world.isRaining();
        boolean isStorming = world.isThundering();
        boolean isFullMoon = state.getCurrentMoonPhase() == 0;
        long currentWorldTicks = world.getTimeOfDay() % 24000;

        for (MineboxFishingShoal.FishingShoalFish fish : fishables) {
            if (!fish.getShoal().equals(shoal))
                continue;
            if (!isTimeInRange(fish, currentWorldTicks))
                continue;

            var conditions = fish.getConditions();
            boolean weatherRequired = Boolean.TRUE.equals(conditions.getRain())
                    || Boolean.TRUE.equals(conditions.getStorm()) || Boolean.TRUE.equals(conditions.getFullMoon());
            boolean weatherMet = (!Boolean.TRUE.equals(conditions.getRain()) || isRaining) &&
                    (!Boolean.TRUE.equals(conditions.getStorm()) || isStorming) &&
                    (!Boolean.TRUE.equals(conditions.getFullMoon()) || isFullMoon);

            if (!weatherRequired || weatherMet) {
                TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
                Identifier resourceID = fish.getResource();
                if (textureExists(textureManager, resourceID)) {
                    textures.add(resourceID);
                }
            }
        }
        return textures;
    }

    private static boolean isTimeInRange(MineboxFishingShoal.FishingShoalFish fish, long currentWorldTicks) {
        int minTime = fish.getTimeRange().get(0);
        int maxTime = fish.getTimeRange().get(1);
        return minTime <= maxTime
                ? currentWorldTicks >= minTime && currentWorldTicks <= maxTime
                : currentWorldTicks >= minTime || currentWorldTicks <= maxTime;
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

    public static Text getEntityText(Entity entity) {
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

    private static String getCachedEntityTextKey(Entity entity) {
        String uuid = entity.getUuid().toString();
        State state = MineboxAdditionsClient.INSTANCE.modState;
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