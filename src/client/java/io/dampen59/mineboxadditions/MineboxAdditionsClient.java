package io.dampen59.mineboxadditions;

import io.dampen59.mineboxadditions.events.*;
import io.dampen59.mineboxadditions.state.State;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class MineboxAdditionsClient implements ClientModInitializer {

	private State modState = new State();

	@Override
	public void onInitializeClient() {
		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

		SocketEvents socketEvents = new SocketEvents(modState);
		ServerEvents serverEvents = new ServerEvents(modState);
		MouseEvent mouseEvent = new MouseEvent(modState);
		BakeryEvent bakeryEvent = new BakeryEvent(modState);
		BuckstarEvent buckstarEvent = new BuckstarEvent(modState);
		CocktailEvent cocktailEvent = new CocktailEvent(modState);
		InventoryEvent inventoryEvent = new InventoryEvent(modState);
		ContainerOpenEvent containerOpenEvent = new ContainerOpenEvent(modState);
		TooltipEvent tooltipEvent = new TooltipEvent(modState);
		SkyEvent skyEvent = new SkyEvent(modState);

	}
}