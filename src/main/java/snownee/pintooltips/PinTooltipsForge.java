package snownee.pintooltips;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PinTooltips.ID, dist = Dist.CLIENT)
public class PinTooltipsForge {

	public PinTooltipsForge() {
		new PinTooltips().onInitializeClient();
		NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) ->
				PinTooltips.onDrag(event.getScreen(), event.getMouseButton(), event.getDragX(), event.getDragY()));
	}
}
