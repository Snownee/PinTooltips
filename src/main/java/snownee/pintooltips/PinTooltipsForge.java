package snownee.pintooltips;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PinTooltips.ID)
public class PinTooltipsForge {

	public PinTooltipsForge() {
		//noinspection removal
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> new PinTooltips().onInitializeClient());
		MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) ->
				PinTooltips.onDrag(event.getScreen(), event.getMouseButton(), event.getDragX(), event.getDragY()));
	}
}
