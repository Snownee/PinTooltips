package snownee.pintooltips;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import snownee.pintooltips.util.Event;

public final class PinTooltipsEvents {
	public static final Event<ScreenInit> SCREEN_INIT = new Event<>(listeners -> screen -> {
		for (ScreenInit listener : listeners) {
			listener.onScreenInit(screen);
		}
	});

	public static final Event<KeyPress> KEY_PRESS = new Event<>(listeners -> (screen, keyCode, scanCode, modifiers) -> {
		for (KeyPress listener : listeners) {
			listener.onKeyPress(screen, keyCode, scanCode, modifiers);
		}
	});

	public static final Event<KeyRelease> KEY_RELEASE = new Event<>(listeners -> (screen, keyCode, scanCode, modifiers) -> {
		for (KeyRelease listener : listeners) {
			listener.onKeyRelease(screen, keyCode, scanCode, modifiers);
		}
	});

	public static final Event<MouseClick> MOUSE_CLICK = new Event<>(listeners -> (screen, mouseX, mouseY, button) -> {
		for (MouseClick listener : listeners) {
			if (!listener.onMouseClick(screen, mouseX, mouseY, button)) {
				return false;
			}
		}
		return true;
	});

	public static final Event<MouseRelease> MOUSE_RELEASE = new Event<>(listeners -> (screen, mouseX, mouseY, button) -> {
		for (MouseRelease listener : listeners) {
			if (!listener.onMouseRelease(screen, mouseX, mouseY, button)) {
				return false;
			}
		}
		return true;
	});

	public static final Event<MouseDrag> MOUSE_DRAG = new Event<>(listeners -> (screen, button, dragX, dragY) -> {
		for (MouseDrag listener : listeners) {
			if (listener.onMouseDrag(screen, button, dragX, dragY)) {
				return true;
			}
		}
		return false;
	});

	public static final Event<PostRender> POST_RENDER = new Event<>(listeners -> (screen, graphics, mouseX, mouseY) -> {
		for (PostRender listener : listeners) {
			listener.onPostRender(screen, graphics, mouseX, mouseY);
		}
	});

	public static final Event<ScreenRemoved> SCREEN_REMOVED = new Event<>(listeners -> screen -> {
		for (ScreenRemoved listener : listeners) {
			listener.onScreenRemoved(screen);
		}
	});

	public static final Event<Disconnect> DISCONNECT = new Event<>(listeners -> () -> {
		for (Disconnect listener : listeners) {
			listener.onDisconnect();
		}
	});

	private PinTooltipsEvents() {
	}

	@FunctionalInterface
	public interface ScreenInit {
		void onScreenInit(Screen screen);
	}

	@FunctionalInterface
	public interface KeyPress {
		void onKeyPress(Screen screen, int keyCode, int scanCode, int modifiers);
	}

	@FunctionalInterface
	public interface KeyRelease {
		void onKeyRelease(Screen screen, int keyCode, int scanCode, int modifiers);
	}

	@FunctionalInterface
	public interface MouseClick {
		boolean onMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface MouseRelease {
		boolean onMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@FunctionalInterface
	public interface MouseDrag {
		boolean onMouseDrag(Screen screen, int button, double dragX, double dragY);
	}

	@FunctionalInterface
	public interface PostRender {
		void onPostRender(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY);
	}

	@FunctionalInterface
	public interface ScreenRemoved {
		void onScreenRemoved(Screen screen);
	}

	@FunctionalInterface
	public interface Disconnect {
		void onDisconnect();
	}
}