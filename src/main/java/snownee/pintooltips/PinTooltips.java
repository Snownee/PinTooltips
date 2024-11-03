package snownee.pintooltips;

import java.io.File;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.slf4j.Logger;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class PinTooltips implements ClientModInitializer {
	public static final String ID = "pin_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static int grabbingTimer = -1;

	public static final KeyMapping GRAB_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.pin_tooltips.pin",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_LALT,
			"key.categories.misc"
	));

	public static File configDirectory = FabricLoader.getInstance().getConfigDir().toFile();
	public static boolean jeed = FabricLoader.getInstance().isModLoaded("jeed");

	@Override
	public void onInitializeClient() {
		var service = PinnedTooltipsService.INSTANCE;
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (PinTooltipsConfig.get().screenBlacklist().contains(screen.getClass().getName())) {
				return;
			}

			ScreenKeyboardEvents.afterKeyPress(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(true);
					if (grabbingTimer < 0) {
						grabbingTimer = 0;
					}
				}
			});

			ScreenKeyboardEvents.afterKeyRelease(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(false);
					grabbingTimer = -1;
				}
			});

			ScreenMouseEvents.allowMouseClick(screen).register((ignored, mouseX, mouseY, button) -> {
				var focused = Suppliers.memoize(() -> service.findHovered(mouseX, mouseY));
				switch (button) {
					case InputConstants.MOUSE_BUTTON_LEFT -> {
						if (focused.get() != null) {
							service.focused = focused.get();
							service.operating = true;
							return false;
						}
					}
					case InputConstants.MOUSE_BUTTON_MIDDLE -> {
						if (focused.get() != null) {
							service.tooltips.remove(focused.get());
							service.operating = true;
							return false;
						}
					}
					case InputConstants.MOUSE_BUTTON_RIGHT -> {
						if (GRAB_KEY.isDown()) {
							service.tooltips.clear();
						}
					}
				}
				return true;
			});

			ScreenMouseEvents.afterMouseRelease(screen).register((ignored, mouseX, mouseY, button) -> {
				service.focused = null;
				service.operating = false;
			});

			ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, tickDelta) -> {
				var font = Minecraft.getInstance().font;
				var zOffset = 1;
				context.pose().pushPose();
				for (var tooltip : service.tooltips) {
					context.pose().translate(0, 0, zOffset);
					zOffset += 200;
					tooltip.render(screen1, font, context, mouseX, mouseY, tickDelta);
				}
				context.pose().popPose();
				if (service.findHovered(mouseX, mouseY) != null) {
					Component hint;
					if (!GRAB_KEY.isUnbound() && System.currentTimeMillis() / 2000 % 2 == 0) {
						hint = Component.translatable("gui.pin_tooltips.clear_hint", GRAB_KEY.getTranslatedKeyMessage());
					} else {
						hint = Component.translatable("gui.pin_tooltips.unpin_hint");
					}
					context.drawCenteredString(font, hint, screen1.width / 2, 4, 0xAAAAAA);
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			service.tooltips.clear();
			service.focused = null;
			service.operating = false;
		});
	}

	public static void onDrag(Screen screen, int button, double mouseX, double mouseY, double deltaX, double deltaY) {
		var service = PinnedTooltipsService.INSTANCE;
		var focused = service.focused;
		if (button == InputConstants.MOUSE_BUTTON_LEFT && focused != null) {
			var position = focused.position();
			focused.setPosition(screen.width, screen.height, position.x() + deltaX, position.y() + deltaY);
		} else if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
			service.tooltips.remove(service.findHovered(mouseX, mouseY));
		}
	}

	public static void onRenderTooltip(
			Font font,
			List<Component> tooltipLines,
			@Nullable TooltipComponent tooltipImage,
			List<ClientTooltipComponent> components,
			int mouseX,
			int mouseY,
			ItemStack itemStack) {
		var service = PinnedTooltipsService.INSTANCE;
		if (grabbingTimer < 0 || service.focused != null || service.operating) {
			return;
		}

		// Throttle 10 ms
		if (((System.currentTimeMillis() / 10) & 1) == 0) {
			return;
		}

		// skip the first frame to skip the deferred tooltip
		if (grabbingTimer++ != 1) {
			return;
		}

		service.tooltips.add(new PinnedTooltip(
				new Vector2d(mouseX, mouseY),
				tooltipLines,
				components,
				Minecraft.getInstance().getWindow().getGuiScaledWidth(),
				Minecraft.getInstance().getWindow().getGuiScaledHeight(),
				font,
				itemStack));
	}

	public static boolean isGrabbing() {
		return grabbingTimer >= 0;
	}
}
