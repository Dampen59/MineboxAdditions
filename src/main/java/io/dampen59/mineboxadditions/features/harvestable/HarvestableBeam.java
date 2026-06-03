package io.dampen59.mineboxadditions.features.harvestable;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.Config;
import io.dampen59.mineboxadditions.config.other.HarvestablesSettings;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;

public class HarvestableBeam {
    private static final Identifier BEACON_BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");

    public static void render(LevelRenderContext context) {
        var mc = Minecraft.getInstance();
        var world = mc.level;
        if (world == null || mc.player == null)
            return;

        // get dim
        Identifier worldId = world.dimension().identifier();
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

        PoseStack ms = context.poseStack();
        MultiBufferSource prov = context.bufferSource();
        if (ms == null || prov == null)
            return;

        float tickDelta = 0f; // render distance API changed in 26.1
        float time = (mc.level.getGameTime() + tickDelta);
        float scroll = (time / 40.0f) % 1.0f;

        // sft cull : as in vanilla mc render
        int rd = 8; // fixed fallback; render distance API changed in 26.1
        double maxDist = (rd * 16 + 64);
        double maxDistSq = maxDist * maxDist;

        Vec3 cam = context.levelState().cameraRenderState.pos;

        ms.pushPose();
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
            Vec3 playerPos = mc.player.position();
            double ndx = x + 0.5 - playerPos.x;
            double ndy = y - playerPos.y;
            double ndz = z + 0.5 - playerPos.z;
            if ((ndx * ndx + ndy * ndy + ndz * ndz) < 16.0)
                continue;

            drawBeaconBeam(ms, prov, new BlockPos(x, y, z), 192, 0.35f, scroll, rgb);
        }

        ms.popPose();
    }

    private static void drawBeaconBeam(PoseStack ms, MultiBufferSource prov, BlockPos base,
                                       int height, float radiusIgnored, float vOffset, int rgb) {
        VertexConsumer vc = prov.getBuffer(RenderTypes.beaconBeam(BEACON_BEAM_TEXTURE, true));
        Matrix4f m = ms.last().pose();

        final float cx = base.getX() + 0.5f;
        final float cz = base.getZ() + 0.5f;
        final float y0 = base.getY();
        final float y1 = y0 + height;
        final float innerR = 0.20f;
        final float outerR = 0.25f;
        final float rot = (float) (vOffset * Math.PI * 2.0);
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

        final int aInner = 200;
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            addBeamSide(vc, m,
                    cx + inner[i][0], cz + inner[i][1],
                    cx + inner[j][0], cz + inner[j][1],
                    y0, y1, u0, v0, u1, v1,
                    cr, cg, cb, aInner);
        }

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
        int light = 0xF000F0; // fullbright

        vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x1, y0, z1).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        vc.addVertex(m, x0, y1, z0).setColor(r, g, b, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }
}
