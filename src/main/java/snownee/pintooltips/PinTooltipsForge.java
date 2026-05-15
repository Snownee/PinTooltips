package snownee.pintooltips;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PinTooltips.ID, dist = Dist.CLIENT)
public class PinTooltipsForge {

	public PinTooltipsForge(IEventBus modBus) {
		PinTooltips.onInitializeClient();

		modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(PinTooltips.GRAB_KEY));

		NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> PinTooltipsEvents.SCREEN_INIT.getInvoker().onScreenInit(event.getScreen()));
		NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyPressed.Pre event) -> PinTooltipsEvents.KEY_PRESS.getInvoker().onKeyPress(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers()));
		NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyReleased.Pre event) -> PinTooltipsEvents.KEY_RELEASE.getInvoker().onKeyRelease(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers()));
		NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonPressed.Pre event) -> {
			if (!PinTooltipsEvents.MOUSE_CLICK.getInvoker().onMouseClick(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
				event.setCanceled(true);
			}
		});
		NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
			if (!PinTooltipsEvents.MOUSE_RELEASE.getInvoker().onMouseRelease(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
				event.setCanceled(true);
			}
		});
		NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseDragged.Pre event) -> {
			if (PinTooltipsEvents.MOUSE_DRAG.getInvoker().onMouseDrag(event.getScreen(), event.getMouseButton(), event.getDragX(), event.getDragY())) {
				event.setCanceled(true);
			}
		});
		NeoForge.EVENT_BUS.addListener((ScreenEvent.Render.Post event) -> PinTooltipsEvents.POST_RENDER.getInvoker().onPostRender(event.getScreen(), (net.minecraft.client.gui.GuiGraphicsExtractor) event.getGuiGraphics(), event.getMouseX(), event.getMouseY()));
		NeoForge.EVENT_BUS.addListener((ScreenEvent.Closing event) -> PinTooltipsEvents.SCREEN_REMOVED.getInvoker().onScreenRemoved(event.getScreen()));
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> PinTooltipsEvents.DISCONNECT.getInvoker().onDisconnect());
	}
}
