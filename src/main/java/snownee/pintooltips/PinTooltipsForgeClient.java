package snownee.pintooltips;

import java.util.Objects;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class PinTooltipsForgeClient {
	public static void init() {
		Objects.requireNonNull(PinTooltips.GRAB_KEY);
		//noinspection removal
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> new PinTooltips().onInitializeClient());
		MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) ->
				PinTooltips.onDrag(event.getScreen(), event.getMouseButton(), event.getDragX(), event.getDragY()));
	}
}
