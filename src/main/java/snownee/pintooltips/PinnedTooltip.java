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
import snownee.pintooltips.mixin.interact.ClientTextTooltipAccess;
import snownee.pintooltips.mixin.pin.GuiGraphicsAccess;
import snownee.pintooltips.util.DummyHoveredSlot;

public final class PinnedTooltip implements ClientTooltipPositioner {
	public static final int TOOLTIP_PADDING = 3;
	private final Vector2d position;
	private final Vector2i size;
	private final List<ClientTooltipComponent> components;
	private final @Nullable DummyHoveredSlot hoveredSlot;
	private final Map<Rect2i, ClientTooltipComponent> linesPosition;

	public PinnedTooltip(
			Vector2d position,
			Vector2i size,
			List<ClientTooltipComponent> components,
			@Nullable DummyHoveredSlot hoveredSlot) {
		this.position = position;
		this.size = size;
		this.components = components;
		this.hoveredSlot = hoveredSlot;
		this.linesPosition = new Reference2ObjectOpenHashMap<>();
	}

	public PinnedTooltip(
			Vector2d position,
			List<ClientTooltipComponent> components,
			int screenWidth,
			int screenHeight,
			Font font,
			ItemStack itemStack
	) {
		this(
				position,
				new Vector2i(),
				components,
				itemStack.isEmpty() ? null : new DummyHoveredSlot(itemStack.copy()));
		updateSize(screenWidth, screenHeight, font);
	}

	public boolean isHovering(double mouseX, double mouseY) {
		return mouseX >= position.x() - TOOLTIP_PADDING && mouseX <= position.x() + size.x() + TOOLTIP_PADDING
				&& mouseY >= position.y() - TOOLTIP_PADDING && mouseY <= position.y() + size.y() + TOOLTIP_PADDING;
	}

	public void updateSize(int screenWidth, int screenHeight, Font font) {
		var width = 0;
		var height = 0;
		linesPosition.clear();
		for (var component : components) {
			var componentWidth = component.getWidth(font);
			var componentHeight = component.getHeight();
			linesPosition.put(new Rect2i(0, height, componentWidth, componentHeight), component);
			width = Math.max(width, componentWidth);
			height += componentHeight;
		}
		if (width != size.x() || height != size.y()) {
			size.set(width, height);
		}
	}

	public void render(PinnedTooltipsService service, Screen screen, Font font, GuiGraphics context, int mouseX, int mouseY) {
		context.pose().pushPose();
		updateSize(screen.width, screen.height, font);
		var inContainer = hoveredSlot() != null && screen instanceof PTContainerScreen;
		if (inContainer) {
			((PTContainerScreen) screen).pin_tooltips$setDummyHoveredSlot(hoveredSlot());
		}

		PTGuiGraphics.of(context).pin_tooltips$setRenderingPinned(true);
		((GuiGraphicsAccess) context).callRenderTooltipInternal(
				font,
				components(),
				(int) position().x(),
				(int) position().y(),
				this);
		PTGuiGraphics.of(context).pin_tooltips$setRenderingPinned(false);

		if (service.hovered == this) {
			var style = getStyleAt(mouseX, mouseY, font);
			if (style != null) {
				PTGuiGraphics.of(context).pin_tooltips$setRenderingPinnedEvent(true);
				context.pose().translate(0, 0, 1);
				context.renderComponentHoverEffect(font, style, mouseX, mouseY);
				PTGuiGraphics.of(context).pin_tooltips$setRenderingPinnedEvent(false);
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
		var relativeX = (int) (mouseX - position().x());
		var relativeY = (int) (mouseY - position().y());
		var line = linesPosition.keySet().stream().filter(rect -> rect.contains(relativeX, relativeY)).findFirst().orElse(null);
		var component = linesPosition.get(line);
		if (component instanceof ClientTextTooltipAccess textTooltip) {
			return font.getSplitter().componentStyleAtWidth(textTooltip.getText(), relativeX);
		}
		return null;
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
}
