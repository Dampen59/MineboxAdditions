package io.dampen59.mineboxadditions.events;

import io.dampen59.mineboxadditions.ModConfig;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;

public class InventoryEvent {
    private State modState = null;

    public InventoryEvent(State prmModState) {
        this.modState = prmModState;
        onTick();
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;
            if (client.world == null) return;
            if (!modState.getConnectedToMinebox()) return;

            client.player.getInventory().offHand.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> {
                handleDurability(stack);
            });

            client.player.getInventory().main.stream().filter(stack -> !stack.isEmpty()).forEach(stack -> {
                handleDurability(stack);
            });

        });
    }

    public void handleDurability(ItemStack prmItemStack) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        NbtComponent itemData = prmItemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (itemData == null) return;

        NbtCompound nbtData = itemData.copyNbt();
        if (nbtData == null || !nbtData.contains("mbitems:id")) return;

        String id = nbtData.getString("mbitems:id");
        LoreComponent loresList = prmItemStack.get(DataComponentTypes.LORE);

        for (Text lore : loresList.lines()) {
            if (!(lore.getContent() instanceof TranslatableTextContent)) continue;

            TranslatableTextContent translatableContent = (TranslatableTextContent) lore.getContent();
            if (id.contains("haversack") && translatableContent.getKey().contains("mbx.items.infinite_bag.amount_inside")) {
                if (config.durabilitySettings.haversackDurability) handleHaversackDurability(prmItemStack, nbtData, translatableContent);
            } else if (id.contains("harvester_") && translatableContent.getKey().contains("mbx.durability")) {
                if (config.durabilitySettings.harvesterDurability) handleHarvesterDurability(prmItemStack, translatableContent);
            }
        }
    }

    private void handleHaversackDurability(ItemStack prmItemStack, NbtCompound nbtData, TranslatableTextContent translatableContent) {
        StringVisitable haversackQuantity = translatableContent.getArg(0);
        String[] quantityParts = haversackQuantity.getString().split("/");
        int haversackMaxQuantity = Integer.parseInt(quantityParts[1]);

        NbtCompound persistentData = nbtData.getCompound("mbitems:persistent");
        int amountInside = persistentData.getInt("mbitems:amount_inside");

        ComponentChanges haversackChanges = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, haversackMaxQuantity - amountInside)
                .add(DataComponentTypes.MAX_DAMAGE, haversackMaxQuantity)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();

        prmItemStack.setDamage(amountInside);
        prmItemStack.applyChanges(haversackChanges);
    }

    private void handleHarvesterDurability(ItemStack prmItemStack, TranslatableTextContent translatableContent) {
        StringVisitable durabilityInfo = translatableContent.getArg(0);
        String durabilityStr = durabilityInfo.getString();

        if (!durabilityStr.contains("/")) return; // Item not repairable

        String[] quantityParts = durabilityStr.split("/");
        int harvesterCurrentDurability = Integer.parseInt(quantityParts[0]);
        int harvesterMaxDurability = Integer.parseInt(quantityParts[1]);

        ComponentChanges harvesterChanges = ComponentChanges.builder()
                .add(DataComponentTypes.DAMAGE, harvesterCurrentDurability)
                .add(DataComponentTypes.MAX_DAMAGE, harvesterMaxDurability)
                .remove(DataComponentTypes.UNBREAKABLE)
                .build();

        prmItemStack.setDamage(harvesterCurrentDurability);
        prmItemStack.applyChanges(harvesterChanges);
    }


}
