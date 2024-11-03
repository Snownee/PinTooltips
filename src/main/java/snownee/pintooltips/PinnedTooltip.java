package snownee.pintooltips;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.duck.PTContainerScreen;

public record PinnedTooltip(
		Vector2d position,
		Vector2i size,
		List<Component> content,
		@Nullable TooltipComponent tooltipImage,
		List<ClientTooltipComponent> components,
		ClientTooltipPositioner positioner,
		Vector2d offset,
		ItemStack itemStack,
		@Nullable DummyHoveredSlot hoveredSlot) {

	private static final int TOOLTIP_PADDING = 4;

	public PinnedTooltip(
			Vector2d position,
			List<Component> content,
			@Nullable TooltipComponent tooltipImage,
			List<ClientTooltipComponent> components,
			ClientTooltipPositioner positioner,
			int screenWidth,
			int screenHeight,
			Font font,
			ItemStack itemStack
	) {
		this(
				position,
				new Vector2i(),
				content,
				tooltipImage,
				components,
				positioner,
				new Vector2d(),
				itemStack,
				itemStack.isEmpty() ? null : new DummyHoveredSlot(itemStack.copy()));
		offset.set(getPositionerOffset(screenWidth, screenHeight, position.x(), position.y()));
		updateSize(screenWidth, screenHeight, font);
	}

	public boolean isHovering(double mouseX, double mouseY) {
		var x = mouseX + offset.x();
		var y = mouseY + offset.y();
		return x >= position.x() - TOOLTIP_PADDING && x <= position.x() + size.x() + TOOLTIP_PADDING
				&& y >= position.y() - TOOLTIP_PADDING && y <= position.y() + size.y() + TOOLTIP_PADDING;
	}

	public void updateSize(int screenWidth, int screenHeight, Font font) {
		var width = 0;
		var height = 0;
		for (var line : components) {
			width = Math.max(width, line.getWidth(font));
			height += line.getHeight();
		}
		if (width != size.x() && height != size.y()) {
			size.set(width, height);
			offset.set(getPositionerOffset(screenWidth, screenHeight, position.x, position.y));
		}
	}

	public Vector2d getPositionerOffset(int screenWidth, int screenHeight, double x, double y) {
		var offset = positioner.positionTooltip(screenWidth, screenHeight, (int) x, (int) y, size.x(), size.y());
		var deltaX = x - offset.x();
		var deltaY = y - offset.y();
		return new Vector2d(deltaX, deltaY);
	}

	public void setPosition(int screenWidth, int screenHeight, double x, double y) {
		var actualX = Math.max(Math.min(x, screenWidth - size.x() + offset.x - TOOLTIP_PADDING), offset.x + TOOLTIP_PADDING);
		var actualY = Math.max(Math.min(y, screenHeight - size.y() + offset.y - TOOLTIP_PADDING), offset.y + TOOLTIP_PADDING);
		offset.set(getPositionerOffset(screenWidth, screenHeight, actualX, actualY));
		position.set(actualX, actualY);
	}

	public void renderPre(Screen screen) {
		if (hoveredSlot != null && screen instanceof PTContainerScreen access) {
			access.pin_tooltips$setDummyHoveredSlot(hoveredSlot);
		}
	}

	public void renderPost(Screen screen) {
		if (hoveredSlot != null && screen instanceof PTContainerScreen access) {
			access.pin_tooltips$setDummyHoveredSlot(null);
		}
	}
}
