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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.mixin.GuiGraphicsAccess;
import snownee.pintooltips.util.SimpleTooltipPositioner;

public class PinTooltips implements ClientModInitializer {
	public static final String ID = "pin_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final ThreadLocal<Boolean> IS_HOLDING_KEY = ThreadLocal.withInitial(() -> false);

	public static final KeyMapping GRAB_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key."+ID+".pin",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_F8,
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

			ScreenKeyboardEvents.afterKeyPress(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(true);
				}
			});

			ScreenKeyboardEvents.afterKeyRelease(screen).register((ignored, key, scancode, modifiers) -> {
				if (GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(false);
					if (service.snapshot != null) {
						service.tooltips.add(service.snapshot);
						service.snapshot = null;
					}
				}
			});

			ScreenMouseEvents.allowMouseClick(screen).register((ignored, mouseX, mouseY, button) -> {
				var focused = service.findFocused(mouseX, mouseY);
				switch (button) {
					case InputConstants.MOUSE_BUTTON_LEFT -> {
						if (focused != null) {
							service.focused = focused;
							service.operating = true;
							service.snapshot = null;
							return false;
						}
					}
					case InputConstants.MOUSE_BUTTON_MIDDLE -> {
						if (focused != null) {
							service.tooltips.remove(focused);
							service.operating = true;
							service.snapshot = null;
							return false;
						}
					}
					case InputConstants.MOUSE_BUTTON_RIGHT -> {
						if (GRAB_KEY.isDown()) {
							service.tooltips.clear();
							service.snapshot = null;
						}
					}
				}
				return true;
			});

			ScreenMouseEvents.afterMouseRelease(screen).register((ignored, mouseX, mouseY, button) -> {
				service.focused = null;
				service.operating = false;
			});

			ScreenEvents.afterRender(screen).register((ignored, context, mouseX, mouseY, tickDelta) -> {
				var font = Minecraft.getInstance().font;
				var zOffset = 1;
				context.pose().pushPose();
				for (var tooltip : service.tooltips) {
					tooltip.updateSize(screen.width, screen.height, font);
					tooltip.renderPre(screen);
					context.pose().translate(0, 0, zOffset);
					zOffset += 200;
					((GuiGraphicsAccess) context).callRenderTooltipInternal(
							font,
							tooltip.components(),
							(int) tooltip.position().x(),
							(int) tooltip.position().y(),
							SimpleTooltipPositioner.INSTANCE);
					tooltip.renderPost(screen);
				}
				context.pose().popPose();
				if (GRAB_KEY.isDown()) {
					context.drawCenteredString(
							Minecraft.getInstance().font,
							Component.translatable("gui.pin_tooltips.holding_info_" + System.currentTimeMillis() / 3000 % 3),
							screen.width / 2,
							4,
							0xAAAAAA);
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			service.tooltips.clear();
			service.focused = null;
			service.operating = false;
			service.snapshot = null;
		});
	}

	public static void onDrag(Screen screen, int button, double mouseX, double mouseY, double deltaX, double deltaY) {
		var service = PinnedTooltipsService.INSTANCE;
		var focused = service.focused;
		if (button == InputConstants.MOUSE_BUTTON_LEFT && focused != null) {
			var position = focused.position();
			focused.setPosition(screen.width, screen.height, position.x() + deltaX, position.y() + deltaY);
		} else if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
			service.tooltips.remove(service.findFocused(mouseX, mouseY));
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
		if (!GRAB_KEY.isDown() || service.focused != null || service.operating) {
			return;
		}

		service.snapshot =
				new PinnedTooltip(
						new Vector2d(mouseX, mouseY),
						tooltipLines,
						tooltipImage,
						components,
						Minecraft.getInstance().getWindow().getGuiScaledWidth(),
						Minecraft.getInstance().getWindow().getGuiScaledHeight(),
						font,
						itemStack);
	}

	public static boolean isHoldingKey() {
		return IS_HOLDING_KEY.get();
	}
}
