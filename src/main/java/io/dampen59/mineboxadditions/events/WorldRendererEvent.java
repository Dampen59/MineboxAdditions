package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.config.render.categories.FishingShoals;
import io.dampen59.mineboxadditions.config.items.ItemsConfig;
import io.dampen59.mineboxadditions.features.fishingshoal.FishingShoalDisplay;
import io.dampen59.mineboxadditions.features.harvestable.HarvestableBeam;
import io.dampen59.mineboxadditions.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

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
            this.pos = pos.immutable();
            this.untilTick = untilTick;
        }
    }

    private static final List<HighlightEntry> ENTRIES = new ArrayList<>();

    public WorldRendererEvent() {
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(WorldRendererEvent::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null)
                return;
            tickCounter++;
            if (tickCounter < 200)
                return;
            tickCounter = 0;

            Set<String> liveUuids = new HashSet<>();
            for (Entity entity : client.level.entitiesForRendering()) {
                liveUuids.add(entity.getUUID().toString());
            }
            MineboxAdditions.INSTANCE.state.cleanStaleEntityTextCache(liveUuids);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!ItemsConfig.rangeDisplay) return InteractionResult.PASS;

            if (!world.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof LocalPlayer)) return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            int itemSize = Utils.getItemSize(player.getItemInHand(InteractionHand.MAIN_HAND));
            if (itemSize == 0) return InteractionResult.PASS;

            Direction facing = player.getDirection();
            BlockPos clicked = hit.getBlockPos();
            BlockPos target = clicked.relative(facing, itemSize);

            long now = world.clockManager().getTotalTicks(world.registryAccess().getOrThrow(WorldClocks.OVERWORLD));
            if (ENTRIES.size() >= MAX_HIGHLIGHTS) {
                ENTRIES.removeFirst();
            }
            ENTRIES.add(new HighlightEntry(target, now + HIGHLIGHT_TICKS));
            return InteractionResult.PASS;
        });

        // TODO: fix this (tool use block highlight)

    }

    public static void render(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (!Utils.isOnMinebox()) return;

        if (FishingShoals.enabled)
            FishingShoalDisplay.handle(context);

        HarvestableBeam.render(context);
    }
}