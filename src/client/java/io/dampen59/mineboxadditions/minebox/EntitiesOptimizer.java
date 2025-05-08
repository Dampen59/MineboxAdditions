package io.dampen59.mineboxadditions.minebox;

import io.dampen59.mineboxadditions.state.State;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntitiesOptimizer {

    private State modState = null;
    public static List<DisplayEntity.TextDisplayEntity> harvestableDisplays = new ArrayList<>();

    public EntitiesOptimizer(State prmModState) {
        this.modState = prmModState;
        onTick();
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!this.modState.getPerformanceMode()) return;

            if (client.player == null || client.world == null) return;

            harvestableDisplays.clear();
            this.modState.getUnrenderedEntities().clear();

            for (var entity : client.world.getEntities()) {

                if (entity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
                    var text = textDisplay.getText();
                    if (text.getContent() instanceof TranslatableTextContent translatable) {
                        if (translatable.getKey().contains("mbx.harvestable") || translatable.getKey().contains("mbx.bestiary")) {
                            harvestableDisplays.add(textDisplay);
                            continue;
                        }
                    }
                    for (var sibling : text.getSiblings()) {
                        if (sibling.getContent() instanceof TranslatableTextContent translatableSibling) {
                            if (translatableSibling.getKey().contains("mbx.harvestable") || translatableSibling.getKey().contains("mbx.bestiary")) {
                                harvestableDisplays.add(textDisplay);
                                break;
                            }
                        }
                    }
                }
            }

            for (DisplayEntity.TextDisplayEntity harvestable : harvestableDisplays) {
                Vec3d pos = harvestable.getPos();

                // Spawn debug armorstand
//                if (!this.modState.getPerformanceModeDebugArmorstands().containsKey(harvestable)) {
//                    ArmorStandEntity armorStand = new ArmorStandEntity(client.world, pos.x, pos.y, pos.z);
//                    armorStand.setInvisible(false);
//                    armorStand.setNoGravity(true);
//                    armorStand.setBoundingBox(new Box(
//                            pos.x - 1.5, pos.y - 3, pos.z - 1.5,
//                            pos.x + 1.5, pos.y + 8, pos.z + 1.5
//                    ));
//                    client.world.addEntity(armorStand);
//                    this.modState.getPerformanceModeDebugArmorstands().put(harvestable, armorStand);
//                }

                Box searchBox = new Box(
                        pos.x - 1.5, pos.y - 3, pos.z - 1.5,
                        pos.x + 1.5, pos.y + 8, pos.z + 1.5
                );

                List<Entity> nearbyEntities = harvestable.getWorld().getOtherEntities(
                        null,
                        searchBox,
                        entity -> true
                );

                List<DisplayEntity.ItemDisplayEntity> nearbyItemDisplays = new ArrayList<>();

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof DisplayEntity.ItemDisplayEntity itemDisplay) {
                        nearbyItemDisplays.add(itemDisplay);
                    }
                }

                nearbyItemDisplays.sort(Comparator.comparingDouble(Entity::getY));

                int toKeep = nearbyItemDisplays.size() / 2;

                for (int i = toKeep; i < nearbyItemDisplays.size(); i++) {
                    this.modState.getUnrenderedEntities().add(nearbyItemDisplays.get(i));
                }

            }


        });
    }

}
