package snownee.pintooltips;

import java.util.List;
import java.util.Objects;

import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.duck.PTGuiGraphics;
import snownee.pintooltips.util.DefaultDescriptions;

public class PinTooltips {
	public static final String ID = "pin_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Component CLICK_TO_COPY = Component.translatable("chat.copy.click").withStyle(ChatFormatting.GRAY);
	public static final HoverEvent CLICK_TO_COPY_EVENT = new HoverEvent.ShowText(CLICK_TO_COPY);
	public static final Identifier AUTOPIN_HIGHLIGHT = id("autopin_highlight");
	private static final ThreadLocal<Boolean> renderingFrameLock = ThreadLocal.withInitial(() -> false);
	private static int keyPressedFrames = -1;
	private static long lastRenderTooltipTime;
	private static int lastMouseX;
	private static int lastMouseY;
	public static long lastMouseMovedTime;
	private static boolean hasTooltipInThisFrame;
	private static boolean listenersRegistered;

	public static final KeyMapping GRAB_KEY = new KeyMapping(
			"key.pin_tooltips.pin",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_LALT,
			KeyMapping.Category.MISC
	);

	private static boolean validateTranslations = PinTooltipsPlatform.isDevelopmentEnvironment();

	public static Identifier id(String id) {
		return Identifier.fromNamespaceAndPath(ID, id);
	}

	public static void onInitializeClient() {
		if (listenersRegistered) {
			return;
		}
		listenersRegistered = true;
		var service = PinTooltipsService.INSTANCE;

		PinTooltipsEvents.SCREEN_INIT.register(screen -> {
			if (isBlacklistedScreen(screen)) {
				return;
			}

			Minecraft mc = Minecraft.getInstance();
			if (validateTranslations && mc.level != null && mc.level.registryAccess().lookup(Registries.ENCHANTMENT).isPresent()) {
				validateTranslations = false;
				validateTranslations();
			}

			lastMouseMovedTime = 0;
		});
		PinTooltipsEvents.KEY_PRESS.register((screen, keyCode, scanCode, modifiers) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
				return;
			}
			if (!GRAB_KEY.isUnbound() && GRAB_KEY.getKey().getValue() == keyCode) {
				GRAB_KEY.setDown(true);
				if (keyPressedFrames < 0) {
					keyPressedFrames = 0;
				}
			}
		});
		PinTooltipsEvents.KEY_RELEASE.register((screen, keyCode, scanCode, modifiers) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
				return;
			}
			PinnedTooltip tooltip = service.autoPinnedTooltip();
			if (tooltip != null && service.focused != tooltip) {
				service.unpin(tooltip);
			}
			if (!GRAB_KEY.isUnbound() && GRAB_KEY.getKey().getValue() == keyCode) {
				GRAB_KEY.setDown(false);
				keyPressedFrames = -1;
			}
		});
		PinTooltipsEvents.MOUSE_CLICK.register((screen, mouseX, mouseY, button) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
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
		PinTooltipsEvents.MOUSE_RELEASE.register((screen, mouseX, mouseY, button) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
				return true;
			}
			var focused = service.focused;
			var dragging = service.dragging;
			service.clearStates();
			PinnedTooltip tooltip = service.autoPinnedTooltip();
			if (tooltip != null && focused != tooltip) {
				service.unpin(tooltip);
			}
			if (focused != null) {
				if (button == InputConstants.MOUSE_BUTTON_LEFT && !dragging) {
					Minecraft mc = Minecraft.getInstance();
					ActiveTextCollector.ClickableStyleFinder collector = new ActiveTextCollector.ClickableStyleFinder(
							mc.font,
							(int) mouseX,
							(int) mouseY);
					focused.layout().visitLines(focused, collector);
					Style style = collector.result();
					if (style == null) {
						style = focused.getExtraStyleAt((int) mouseX, (int) mouseY, mc.font);
					}
				}
			}
			return focused == null;
		});
		PinTooltipsEvents.MOUSE_DRAG.register((screen, button, dragX, dragY) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
				return false;
			}
			var focused = service.focused;
			if (button == InputConstants.MOUSE_BUTTON_LEFT && focused != null) {
				if (!service.dragging) {
					service.storedDragX += dragX;
					service.storedDragY += dragY;
					if (Math.abs(service.storedDragX) + Math.abs(service.storedDragY) > 5) {
						service.dragging = true;
						dragX = service.storedDragX;
						dragY = service.storedDragY;
					}
				}
				if (service.dragging) {
					var position = focused.position();
					focused.setPosition(screen.width, screen.height, position.x() + dragX, position.y() + dragY);
				}
				return true;
			}
			if (button == InputConstants.MOUSE_BUTTON_MIDDLE && service.hovered != null) {
				service.unpin(service.hovered);
			}
			return false;
		});
		PinTooltipsEvents.POST_RENDER.register((screen, context, mouseX, mouseY) -> {
			if (isBlacklistedScreen(screen) || !shouldShowTooltips(screen)) {
				return;
			}
			Minecraft mc = Minecraft.getInstance();
			mouseX = (int) (
					mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth());
			mouseY = (int) (
					mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight());
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
			var font = mc.font;
			for (var tooltip : service.tooltips()) {
				context.pose().pushMatrix();
				tooltip.render(service, screen, font, context, mouseX, mouseY);
				context.pose().popMatrix();
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
				context.centeredText(font, hint, screen.width / 2, 4, 0xAAAAAAAA);

				if (screen instanceof AbstractContainerScreen<?> containerScreen) {
					containerScreen.extractCarriedItem(context, mouseX, mouseY);
				}
			}
		});
		PinTooltipsEvents.SCREEN_REMOVED.register(screen -> {
			if (isBlacklistedScreen(screen)) {
				return;
			}
			PinnedTooltip tooltip = service.autoPinnedTooltip();
			if (tooltip != null) {
				service.unpin(tooltip);
			}
		});
		PinTooltipsEvents.DISCONNECT.register(service::clearStates);
	}

	private static boolean isBlacklistedScreen(Screen screen) {
		return PinTooltipsConfig.screenBlacklist.contains(screen.getClass().getName());
	}

	private static void validateTranslations() {
		LOGGER.info("Validating translations...");
		boolean oHide = PinTooltipsConfig.hideMissingDescriptions;
		PinTooltipsConfig.hideMissingDescriptions = true;
		RegistryAccess registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
		List<Identifier> missingEnchantments = Lists.newArrayList();
		for (Holder.Reference<Enchantment> holder : registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements().toList()) {
			if (DefaultDescriptions.forEnchantmentRaw(holder) == null) {
				missingEnchantments.add(holder.key().identifier());
			}
		}
		if (!missingEnchantments.isEmpty()) {
			String msg = "Missing enchantment descriptions: %s".formatted(missingEnchantments);
			Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(msg).withStyle(ChatFormatting.DARK_RED), false);
		}
		List<Identifier> missingEffects = Lists.newArrayList();
		for (Holder.Reference<MobEffect> holder : registryAccess.lookupOrThrow(Registries.MOB_EFFECT).listElements().toList()) {
			if (DefaultDescriptions.forStatusEffectRaw(holder.value()) == null) {
				missingEffects.add(holder.key().identifier());
			}
		}
		if (!missingEffects.isEmpty()) {
			String msg = "Missing status effect descriptions: %s".formatted(missingEffects);
			Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(msg).withStyle(ChatFormatting.DARK_RED), false);
		}
		PinTooltipsConfig.hideMissingDescriptions = oHide;
		LOGGER.info("Translations validated.");
	}

	public static void onRenderTooltip(
			GuiGraphicsExtractor graphics,
			Font font,
			List<ClientTooltipComponent> components,
			@Nullable Identifier style,
			Vector2ic position,
			@Nullable TooltipLayout layout) {
		var service = PinTooltipsService.INSTANCE;
		if (service.focused != null) {
			return;
		}

		PTGuiGraphics context = PTGuiGraphics.of(graphics);
		if (context.pin_tooltips$getRenderingPinned()) {
			return;
		}

		ItemStack itemStack = context.pin_tooltips$getRenderingItemStack();
		ClientTooltipComponent image = context.pin_tooltips$getRenderingImage();
		long time = System.currentTimeMillis();

		if (keyPressedFrames < 0) {
			int delay = PinTooltipsConfig.hoveringAutoPinDelay;
			if (delay >= 0) {
				hasTooltipInThisFrame = true;
				if (lastMouseMovedTime > 0 && time - lastMouseMovedTime >= delay) {
					service.pin(layout, position, font, components, style, itemStack, image, time);
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

		service.pin(layout, position, font, components, style, itemStack, image, -1);
	}

	public static void onRenderFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height, @Nullable Identifier style) {
		if (PinTooltips.isGrabbing() || PTGuiGraphics.of(graphics).pin_tooltips$getRenderingPinned()) {
			return;
		}
		if (renderingFrameLock.get()) {
			return;
		}
		int delay = PinTooltipsConfig.hoveringAutoPinDelay;
		if (lastMouseMovedTime == 0 || delay <= 0) {
			return;
		}
		float alpha = Mth.clamp((float) (System.currentTimeMillis() - lastMouseMovedTime) / delay, 0, 1);
		if (alpha < 0.2F) {
			return;
		}
		renderingFrameLock.set(true);
		renderTooltipFrame(graphics, x, y, width, height, AUTOPIN_HIGHLIGHT, alpha);
		renderingFrameLock.set(false);
	}

	public static boolean isGrabbing() {
		int delay = PinTooltipsConfig.hoveringAutoPinDelay;
		return GRAB_KEY.isDown() || delay >= 0 && lastMouseMovedTime > 0 && System.currentTimeMillis() - lastMouseMovedTime >= delay;
	}

	public static boolean shouldShowTooltips(Screen screen) {
		// Avoid rendering tooltips when there are multiple screens open
		return Minecraft.getInstance().screen == screen;
	}

	public static void renderTooltipFrame(
			GuiGraphicsExtractor graphics,
			int x,
			int y,
			int w,
			int h,
			@Nullable Identifier style,
			float alpha) {
	}
}
