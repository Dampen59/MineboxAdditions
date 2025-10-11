package io.dampen59.mineboxadditions.features.harvestable;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.other.HarvestablesSettings;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;

public class HarvestableBeam {
    private static final Identifier BEACON_BEAM_TEXTURE = Identifier.of("textures/entity/beacon_beam.png");

    public static void render(WorldRenderContext context) {
        var mc = MinecraftClient.getInstance();
        var world = context.world();
        if (world == null || mc.player == null)
            return;

        // get dim
        Identifier worldId = world.getRegistryKey().getValue();
        String islandKeyPath = worldId.getPath();

        var state = MineboxAdditions.INSTANCE.state;
        List<Harvestable> items = state.getMineboxHarvestables(islandKeyPath);
        if (items == null || items.isEmpty()) {
            items = state.getMineboxHarvestables(worldId.toString());
            if (items == null || items.isEmpty())
                return;
        }

        HarvestablesSettings.Harvestable prefs = Config.harvestables.harvestables.get(islandKeyPath);
        if (prefs == null)
            return;

        MatrixStack ms = context.matrixStack();
        VertexConsumerProvider prov = context.consumers();
        if (ms == null || prov == null)
            return;

        float tickDelta = context.tickCounter().getTickProgress(false);
        float time = (mc.world.getTime() + tickDelta);
        float scroll = (time / 40.0f) % 1.0f;

        // sft cull : as in vanilla mc render
        int rd = mc.options.getViewDistance().getValue();
        double maxDist = (rd * 16 + 64);
        double maxDistSq = maxDist * maxDist;

        Vec3d cam = context.camera().getPos();

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);

        for (Harvestable it : items) {
            String cat = it.getCategory() != null ? it.getCategory() : "misc";
            String name = it.getName() != null ? it.getName() : "unknown";

            boolean catOn = prefs.categories.getOrDefault(cat, false);
            if (!catOn)
                continue;

            boolean itemOn = prefs.items
                    .getOrDefault(cat, Collections.emptyMap())
                    .getOrDefault(name, false);
            if (!itemOn)
                continue;

            int rgb = prefs.colors
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
        VertexConsumer vc = prov.getBuffer(RenderLayer.getBeaconBeam(BEACON_BEAM_TEXTURE, true));
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
}