package snownee.pintooltips;


import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(PinTooltips.ID)
public class PinTooltipsForge {

	public PinTooltipsForge() {
		if (FMLEnvironment.dist.isClient()) {
			new PinTooltips().onInitializeClient();
		}
		NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) ->
				PinTooltips.onDrag(event.getScreen(), event.getMouseButton(), event.getDragX(), event.getDragY()));
	}
}
