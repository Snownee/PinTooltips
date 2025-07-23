package snownee.pintooltips;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.joml.Vector2ic;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
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
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.util.DefaultDescriptions;

public class PinTooltips implements ClientModInitializer {
	public static final String ID = "pin_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Component CLICK_TO_COPY = Component.translatable("chat.copy.click").withStyle(ChatFormatting.GRAY);
	public static final HoverEvent CLICK_TO_COPY_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_TO_COPY);
	private static int keyPressedFrames = -1;
	private static long lastRenderTooltipTime;
	private static int lastMouseX;
	private static int lastMouseY;
	public static long lastMouseMovedTime;
	private static boolean hasTooltipInThisFrame;

	public static final KeyMapping GRAB_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.pin_tooltips.pin",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_LALT,
			"key.categories.misc"
	));

	public static File configDirectory = FabricLoader.getInstance().getConfigDir().toFile();
	private static boolean validateTranslations = FabricLoader.getInstance().isDevelopmentEnvironment();

	public static int getMaxZOffset() {
		return 6000;
	}

	@Override
	public void onInitializeClient() {
		PinTooltipsConfig.save();
		var service = PinnedTooltipsService.INSTANCE;
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (PinTooltipsConfig.get().screenBlacklist().contains(screen.getClass().getName())) {
				return;
			}

			if (validateTranslations && client.level != null &&
					client.level.registryAccess().registry(Registries.ENCHANTMENT).isPresent()) {
				validateTranslations = false;
				validateTranslations();
			}

			lastMouseMovedTime = 0;

			ScreenKeyboardEvents.beforeKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
				if (shouldShowTooltips(screen1) && GRAB_KEY.matches(key, scancode)) {
					GRAB_KEY.setDown(true);
					if (keyPressedFrames < 0) {
						keyPressedFrames = 0;
					}
				}
			});

			ScreenKeyboardEvents.afterKeyRelease(screen).register((screen1, key, scancode, modifiers) -> {
				if (shouldShowTooltips(screen1)) {
					if (service.autoPinnedTooltip() != null && service.focused != service.autoPinnedTooltip()) {
						service.unpin(service.autoPinnedTooltip());
					}
					if (GRAB_KEY.matches(key, scancode)) {
						GRAB_KEY.setDown(false);
						keyPressedFrames = -1;
					}
				}
			});

			ScreenMouseEvents.allowMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
				if (!shouldShowTooltips(screen1)) {
					return true;
				}
				if (button != InputConstants.MOUSE_BUTTON_LEFT && button != InputConstants.MOUSE_BUTTON_MIDDLE) {
					return true;
				}
				if (button == InputConstants.MOUSE_BUTTON_MIDDLE && GRAB_KEY.isDown()) {
					service.clearTooltips();
					return false;
				}
				var focused = service.hovered;
				if (focused != null) {
					if (button == InputConstants.MOUSE_BUTTON_LEFT) {
						service.focused = focused;
						service.placeOnTop(focused);
					} else {
						service.unpin(focused);
					}
					return false;
				}
				return true;
			});

			ScreenMouseEvents.allowMouseRelease(screen).register((screen1, mouseX, mouseY, button) -> {
				if (!shouldShowTooltips(screen1)) {
					return true;
				}
				var focused = service.focused;
				var dragging = service.dragging;
				service.clearStates();
				if (service.autoPinnedTooltip() != null && focused != service.autoPinnedTooltip()) {
					service.unpin(service.autoPinnedTooltip());
				}
				if (focused != null) {
					if (button == InputConstants.MOUSE_BUTTON_LEFT && !dragging) {
						Style style = focused.getStyleAt(mouseX, mouseY, Minecraft.getInstance().font);
						if (style != null) {
							screen1.handleComponentClicked(style);
						}
					}
					return false;
				}
				return true;
			});

			ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, tickDelta) -> {
				if (!shouldShowTooltips(screen1)) {
					return;
				}
				if (hasTooltipInThisFrame) {
					hasTooltipInThisFrame = false;
					if (lastMouseX != mouseX || lastMouseY != mouseY) {
						lastMouseX = mouseX;
						lastMouseY = mouseY;
						lastMouseMovedTime = System.currentTimeMillis();
					}
				} else {
					lastMouseX = 0;
					lastMouseY = 0;
					lastMouseMovedTime = 0;
				}

				service.hovered = service.findHovered(mouseX, mouseY);
				var font = Minecraft.getInstance().font;
				var zOffset = 1;
				for (var tooltip : service.tooltips()) {
					context.pose().pushPose();
					context.pose().translate(0, 0, zOffset);
					tooltip.render(service, screen1, font, context, mouseX, mouseY);
					context.pose().popPose();
					zOffset = Math.min(getMaxZOffset() - 1, zOffset + 400);
				}
				PinnedTooltip autoPinnedTooltip = service.autoPinnedTooltip();
				if (autoPinnedTooltip != null && autoPinnedTooltip.isHovered() && autoPinnedTooltip != service.hovered) {
					service.unpin(autoPinnedTooltip);
				}
				if (service.hovered != null) {
					service.hovered.hovered();
					Component hint;
					if (!GRAB_KEY.isUnbound() && System.currentTimeMillis() / 2000 % 2 == 0) {
						hint = Component.translatable("gui.pin_tooltips.clear_hint", GRAB_KEY.getTranslatedKeyMessage());
					} else {
						hint = Component.translatable("gui.pin_tooltips.unpin_hint");
					}
					context.drawCenteredString(font, hint, screen1.width / 2, 4, 0xAAAAAA);
				}
			});

			ScreenEvents.remove(screen).register(screen1 -> {
				PinnedTooltip tooltip = service.autoPinnedTooltip();
				if (tooltip != null) {
					service.unpin(tooltip);
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> service.clearStates());
	}

	private static void validateTranslations() {
		LOGGER.info("Validating translations...");
		PinTooltipsConfig.setOverride(new PinTooltipsConfig(true, false, false, 1500, Set.of()));
		RegistryAccess registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
		List<ResourceLocation> missingEnchantments = Lists.newArrayList();
		for (Holder.Reference<Enchantment> holder : registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().toList()) {
			if (DefaultDescriptions.forEnchantmentRaw(holder) == null) {
				missingEnchantments.add(holder.key().location());
			}
		}
		if (!missingEnchantments.isEmpty()) {
			String msg = "Missing enchantment descriptions: %s".formatted(missingEnchantments);
			Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(msg).withStyle(ChatFormatting.DARK_RED), false);
		}
		List<ResourceLocation> missingEffects = Lists.newArrayList();
		for (Holder.Reference<MobEffect> holder : registryAccess.registryOrThrow(Registries.MOB_EFFECT).holders().toList()) {
			if (DefaultDescriptions.forStatusEffectRaw(holder.value()) == null) {
				missingEffects.add(holder.key().location());
			}
		}
		if (!missingEffects.isEmpty()) {
			String msg = "Missing status effect descriptions: %s".formatted(missingEffects);
			Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(msg).withStyle(ChatFormatting.DARK_RED), false);
		}
		PinTooltipsConfig.setOverride(null);
		LOGGER.info("Translations validated.");
	}

	public static void onDrag(Screen screen, int button, double deltaX, double deltaY) {
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
			service.unpin(service.hovered);
		}
	}

	public static void onRenderTooltip(
			Font font,
			List<ClientTooltipComponent> components,
			Vector2ic position,
			ItemStack itemStack) {
		var service = PinnedTooltipsService.INSTANCE;
		if (service.focused != null) {
			return;
		}

		long time = System.currentTimeMillis();

		if (keyPressedFrames < 0) {
			int delay = PinTooltipsConfig.get().hoveringAutoPinDelay();
			if (delay >= 0) {
				hasTooltipInThisFrame = true;
				if (lastMouseMovedTime > 0 && time - lastMouseMovedTime >= delay) {
					service.pin(position, components, font, itemStack, time);
				}
			}
			return;
		}

		// there can be multiple renderTooltip calls in a single frame, so we need to skip some
		if (time - lastRenderTooltipTime < 10) {
			return;
		}
		lastRenderTooltipTime = time;

		// skip the first frame to skip the deferred tooltip
		if (keyPressedFrames++ != 1) {
			return;
		}

		service.pin(position, components, font, itemStack, -1);
	}

	public static boolean isGrabbing() {
		int delay = PinTooltipsConfig.get().hoveringAutoPinDelay();
		return GRAB_KEY.isDown() || delay >= 0 && lastMouseMovedTime > 0 && System.currentTimeMillis() - lastMouseMovedTime >= delay;
	}

	public static boolean shouldShowTooltips(Screen screen) {
		return Minecraft.getInstance().screen == screen;
	}
}
