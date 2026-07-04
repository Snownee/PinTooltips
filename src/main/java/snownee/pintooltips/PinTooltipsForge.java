package snownee.pintooltips;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = PinTooltips.ID, dist = Dist.CLIENT)
public class PinTooltipsForge {

	public PinTooltipsForge(IEventBus modBus) {
		PinTooltips.onInitializeClient();

		modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(PinTooltips.GRAB_KEY));
	}
}
