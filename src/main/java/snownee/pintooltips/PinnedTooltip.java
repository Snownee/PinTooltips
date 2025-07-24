package snownee.pintooltips;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.duck.PTContainerScreen;
import snownee.pintooltips.duck.PTGuiGraphics;
import snownee.pintooltips.mixin.pin.GuiGraphicsAccess;
import snownee.pintooltips.util.DummyHoveredSlot;

public final class PinnedTooltip implements ClientTooltipPositioner {
	private final TooltipStyle style;
	private final Vector2d position;
	private final Vector2i size;
	private final List<ClientTooltipComponent> components;
	private final @Nullable DummyHoveredSlot hoveredSlot;
	private final Map<Rect2i, ClientTooltipComponent> linesPosition;
	long autoPinnedTimestamp;
	private boolean hovered;

	public PinnedTooltip(
			TooltipStyle style,
			Vector2d position,
			Vector2i size,
			List<ClientTooltipComponent> components,
			long autoPinnedTimestamp,
			@Nullable DummyHoveredSlot hoveredSlot) {
		this.style = style;
		this.position = position;
		this.size = size;
		this.components = components;
		this.autoPinnedTimestamp = autoPinnedTimestamp;
		this.hoveredSlot = hoveredSlot;
		this.linesPosition = new Reference2ObjectOpenHashMap<>();
	}

	public PinnedTooltip(
			TooltipStyle style,
			Vector2d position,
			List<ClientTooltipComponent> components,
			int screenWidth,
			int screenHeight,
			Font font,
			ItemStack itemStack,
			long autoPinnedTimestamp
	) {
		this(
				style,
				position,
				new Vector2i(),
				components,
				autoPinnedTimestamp,
				itemStack.isEmpty() ? null : new DummyHoveredSlot(itemStack.copy()));
		updateSize(screenWidth, screenHeight, font);
	}

	public boolean isHovering(double mouseX, double mouseY) {
		return style.isHovering(this, mouseX, mouseY);
	}

	public void updateSize(int screenWidth, int screenHeight, Font font) {
		style.updateSize(this, screenWidth, screenHeight, font);
	}

	public void render(PinnedTooltipsService service, Screen screen, Font font, GuiGraphics context, int mouseX, int mouseY) {
		context.pose().pushPose();
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
		((GuiGraphicsAccess) context).callRenderTooltipInternal(
				font,
				components(),
				(int) position().x(),
				(int) position().y(),
				this);
		graphics.pin_tooltips$setRenderingPinned(false);

		if (service.hovered == this && !service.dragging) {
			var style = getStyleAt(mouseX, mouseY, font);
			if (style != null) {
				graphics.pin_tooltips$setRenderingPinnedEvent(true);
				context.pose().translate(0, 0, 1);
				context.renderComponentHoverEffect(font, style, mouseX, mouseY);
				graphics.pin_tooltips$setRenderingPinnedEvent(false);
			}
		}

		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$dropDummyHoveredSlot();
		}
		context.pose().popPose();
	}

	public void setPosition(int screenWidth, int screenHeight, double x, double y) {
		position.set(x, y);
	}

	public Vector2d position() {return position;}

	public Vector2ic size() {return size;}

	public List<ClientTooltipComponent> components() {return components;}

	public @Nullable DummyHoveredSlot hoveredSlot() {return hoveredSlot;}

	public @Nullable Style getStyleAt(double mouseX, double mouseY, Font font) {
		return style.getStyleAt(this, mouseX, mouseY, font);
	}

	@Override
	public @NotNull Vector2ic positionTooltip(
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY,
			int tooltipWidth,
			int tooltipHeight) {
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

	public TooltipStyle style() {
		return style;
	}

	public void setSize(int width, int height) {
		size.set(width, height);
	}
}
