package io.dampen59.mineboxadditions;

import io.dampen59.mineboxadditions.events.ServerEvents;
import io.dampen59.mineboxadditions.events.inventory.InventoryEvent;
import io.dampen59.mineboxadditions.events.ContainerOpenEvent;
import io.dampen59.mineboxadditions.events.TooltipEvent;
import io.dampen59.mineboxadditions.events.SkyEvent;
import io.dampen59.mineboxadditions.events.shop.ShopEventManager;
import io.dampen59.mineboxadditions.network.SocketManager;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class MineboxAdditionsClient implements ClientModInitializer {

	private State modState = new State();

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		new SocketManager(modState);
		new ServerEvents(modState);
		new ShopEventManager(modState);
		new InventoryEvent(modState);
		new ContainerOpenEvent(modState);
		new TooltipEvent(modState);
		new SkyEvent(modState);
	}
}
