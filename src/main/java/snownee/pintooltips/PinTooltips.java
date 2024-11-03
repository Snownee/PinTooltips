package snownee.pintooltips;

import java.io.File;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.slf4j.Logger;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class PinTooltips implements ClientModInitializer {
	public static final String ID = "pin_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Component CLICK_TO_COPY = Component.translatable("chat.copy.click").withStyle(ChatFormatting.GRAY);
	public static final HoverEvent CLICK_TO_COPY_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_TO_COPY);
	private static int keyPressedFrames = -1;
	private static long lastRenderTooltipTime;

	public static final KeyMapping GRAB_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.pin_tooltips.pin",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_LALT,
			"key.categories.misc"
	));

	public static File configDirectory = FabricLoader.getInstance().getConfigDir().toFile();

	@Override
	public void onInitializeClient() {
		var service = PinnedTooltipsService.INSTANCE;
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (PinTooltipsConfig.get().screenBlacklist().contains(screen.getClass().getName())) {
				return;
			}
			if (PinTooltipsConfig.get().onlyInContainerScreen() && !(screen instanceof AbstractContainerScreen)) {
				return;
			}

			ScreenKeyboardEvents.afterKeyPress(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(true);
					if (keyPressedFrames < 0) {
						keyPressedFrames = 0;
					}
				}
			});

			ScreenKeyboardEvents.afterKeyRelease(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(false);
					keyPressedFrames = -1;
				}
			});

			ScreenMouseEvents.allowMouseClick(screen).register((ignored, mouseX, mouseY, button) -> {
				if (button != InputConstants.MOUSE_BUTTON_LEFT && button != InputConstants.MOUSE_BUTTON_MIDDLE) {
					return true;
				}
				if (button == InputConstants.MOUSE_BUTTON_MIDDLE && GRAB_KEY.isDown()) {
					service.tooltips.clear();
					return false;
				}
				PinnedTooltip focused = service.findHovered(mouseX, mouseY);
				if (focused != null) {
					if (button == InputConstants.MOUSE_BUTTON_LEFT) {
						service.focused = focused;
					} else {
						service.tooltips.remove(focused);
					}
					service.operating = true;
					return false;
				}
				return true;
			});

			ScreenMouseEvents.afterMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
				PinnedTooltip focused = service.dragging ? null : service.focused;
				service.clearStates();
				if (button == InputConstants.MOUSE_BUTTON_LEFT && focused != null) {
					Style style = focused.getStyleAt(mouseX, mouseY, Minecraft.getInstance().font);
					if (style != null) {
						screen1.handleComponentClicked(style);
					}
				}
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

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> service.clearStates());
	}

	public static void onDrag(Screen screen, int button, double mouseX, double mouseY, double deltaX, double deltaY) {
		var service = PinnedTooltipsService.INSTANCE;
		var focused = service.focused;
		if (button == InputConstants.MOUSE_BUTTON_LEFT && focused != null) {
			if (!service.dragging) {
				service.storedDragX += deltaX;
				service.storedDragY += deltaY;
				if (Math.abs(service.storedDragX) + Math.abs(service.storedDragY) > 5) {
					service.dragging = true;
					deltaX = service.storedDragX;
					deltaY = service.storedDragY;
				}
			}
			if (service.dragging) {
				var position = focused.position();
				focused.setPosition(screen.width, screen.height, position.x() + deltaX, position.y() + deltaY);
			}
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
		if (keyPressedFrames < 0 || service.focused != null || service.operating) {
			return;
		}

		// there can be multiple renderTooltip calls in a single frame, so we need to skip some
		long time = System.currentTimeMillis();
		if (time - lastRenderTooltipTime < 10) {
			return;
		}
		lastRenderTooltipTime = time;

		// skip the first frame to skip the deferred tooltip
		if (keyPressedFrames++ != 1) {
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
		return keyPressedFrames >= 0;
	}
}
