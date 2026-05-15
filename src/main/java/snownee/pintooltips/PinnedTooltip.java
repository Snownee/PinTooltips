package snownee.pintooltips;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.duck.PTContainerScreen;
import snownee.pintooltips.duck.PTGuiGraphics;
import snownee.pintooltips.util.DummyHoveredSlot;

public final class PinnedTooltip implements ClientTooltipPositioner {
	private final TooltipLayout layout;
	private final Vector2d position;
	private final Vector2i size;
	private final List<ClientTooltipComponent> components;
	private final @Nullable Identifier style;
	private final ItemStack itemStack;
	private @Nullable ClientTooltipComponent image;
	private final @Nullable DummyHoveredSlot hoveredSlot;
	private final Map<Rect2i, ClientTooltipComponent> linesPosition;
	long autoPinnedTimestamp;
	private boolean hovered;

	public PinnedTooltip(
			TooltipLayout layout,
			Vector2d position,
			Vector2i size,
			List<ClientTooltipComponent> components,
			@Nullable Identifier style,
			long autoPinnedTimestamp,
			ItemStack itemStack,
			@Nullable ClientTooltipComponent image,
			@Nullable DummyHoveredSlot hoveredSlot) {
		this.layout = layout;
		this.position = position;
		this.size = size;
		this.components = new ArrayList<>(components);
		this.style = style;
		this.autoPinnedTimestamp = autoPinnedTimestamp;
		this.itemStack = itemStack;
		this.image = image;
		this.hoveredSlot = hoveredSlot;
		this.linesPosition = new Reference2ObjectLinkedOpenHashMap<>();
	}

	public PinnedTooltip(
			TooltipLayout layout,
			Vector2d position,
			List<ClientTooltipComponent> components,
			@Nullable Identifier style,
			int screenWidth,
			int screenHeight,
			Font font,
			ItemStack itemStack,
			@Nullable ClientTooltipComponent image,
			long autoPinnedTimestamp) {
		this(
				layout,
				position,
				new Vector2i(),
				components,
				style,
				autoPinnedTimestamp,
				itemStack,
				image,
				itemStack.isEmpty() ? null : new DummyHoveredSlot(itemStack.copy()));
		updateSize(screenWidth, screenHeight, font);
	}

	public boolean isHovering(double mouseX, double mouseY) {
		return layout.isHovering(this, mouseX, mouseY);
	}

	public void updateSize(int screenWidth, int screenHeight, Font font) {
		if (image != null) {
			int i = components.indexOf(image);
			TooltipComponent newImage = itemStack.getTooltipImage().orElse(null);
			if (i != -1 && newImage != null) {
				image = ClientTooltipComponent.create(newImage);
				components.set(i, image);
			}
		}
		layout.updateSize(this, screenWidth, screenHeight, font);
	}

	public void render(PinTooltipsService service, Screen screen, Font font, GuiGraphicsExtractor context, int mouseX, int mouseY) {
		context.pose().pushMatrix();
		updateSize(screen.width, screen.height, font);
		var inContainer = hoveredSlot() != null && screen instanceof PTContainerScreen;
		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$setDummyHoveredSlot(hoveredSlot());
		}

		PTGuiGraphics graphics = PTGuiGraphics.of(context);
		graphics.pin_tooltips$setRenderingPinned(true);
		if (hoveredSlot != null) {
			graphics.pin_tooltips$setRenderingItemStack(hoveredSlot.getItem());
		}
		context.tooltip(font, components(), (int) position().x(), (int) position().y(), this, style);
		graphics.pin_tooltips$setRenderingPinned(false);

		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$dropDummyHoveredSlot();
		}

		if (service.hovered == this && !service.dragging) {
			layout.visitLines(this, context.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR));
			var style = context.hoveredTextStyle;
			if (style == null) {
				style = getExtraStyleAt(mouseX, mouseY, font);
			}
			if (style != null) {
				graphics.pin_tooltips$setRenderingPinnedEvent(true);
				context.extractDeferredElements(mouseX, mouseY, 0);
				graphics.pin_tooltips$setRenderingPinnedEvent(false);
			}
		}
		context.pose().popMatrix();
	}

	public void setPosition(int screenWidth, int screenHeight, double x, double y) {
		position.set(x, y);
	}

	public Vector2d position() {
		return position;
	}

	public Vector2ic size() {
		return size;
	}

	public List<ClientTooltipComponent> components() {
		return components;
	}

	public @Nullable DummyHoveredSlot hoveredSlot() {
		return hoveredSlot;
	}

	public @Nullable Style getExtraStyleAt(double mouseX, double mouseY, Font font) {
		var relativeX = (int) (mouseX - position.x());
		var relativeY = (int) (mouseY - position.y());
		var line = linesPosition.keySet().stream().filter(rect -> rect.contains(relativeX, relativeY)).findFirst();
		var component = line.map(linesPosition::get).orElse(null);
		if (component != null) {
			Style style = getExtraStyleAt(component, font, relativeX - line.get().getX(), relativeY - line.get().getY());
			if (style != null) {
				return style;
			}
		}
		return layout.getExtraStyleAt(this, mouseX, mouseY, font);
	}

	private @Nullable Style getExtraStyleAt(ClientTooltipComponent component, Font font, int relativeX, int relativeY) {
		//noinspection unchecked
		return StyleHandlers.get(component)
				.map($ -> ((StyleHandler<ClientTooltipComponent>) $).handle(this, component, font, relativeX, relativeY, size.x))
				.orElse(null);
	}

	@Override
	public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
		return new Vector2i((int) position.x, (int) position.y);
	}

	public void hovered() {
		hovered = true;
	}

	public boolean isHovered() {
		return hovered;
	}

	public Map<Rect2i, ClientTooltipComponent> linesPosition() {
		return linesPosition;
	}

	public TooltipLayout layout() {
		return layout;
	}

	public void setSize(int width, int height) {
		size.set(width, height);
	}

	public ItemStack itemReference() {
		return itemStack;
	}

	public @Nullable Slot itemSlot() {
		if (!itemStack.isEmpty() && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
			for (Slot slot : screen.getMenu().slots) {
				if (slot.getItem() == itemStack) {
					return slot;
				}
			}
		}
		return null;
	}
}
