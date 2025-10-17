package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.huds.categories.FishingDrops;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoalDisplay;
import io.dampen59.mineboxadditions.features.harvestable.HarvestableBeam;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;


public class WorldRendererEvent {
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
        WorldRenderEvents.AFTER_ENTITIES.register(WorldRendererEvent::render);

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
            MineboxAdditions.INSTANCE.state.cleanStaleEntityTextCache(liveUuids);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
//            if (!MineboxAdditionConfig.get().displaySettings.displayItemRange) return ActionResult.PASS;

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

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!Utils.isOnMinebox()) return;

        if (FishingDrops.enabled)
            FishingShoalDisplay.handle(context);

        HarvestableBeam.render(context);
    }
}