package snownee.pintooltips;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2ic;

import com.google.common.base.Objects;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PinnedTooltip(
		Vector2d position,
		Vector2ic size,
		List<Component> content,
		@Nullable TooltipComponent tooltipImage,
		List<ClientTooltipComponent> components,
		ClientTooltipPositioner positioner,
		Vector2d offset) {

	private static final int TOOLTIP_PADDING = 4;

	public PinnedTooltip(
			Vector2d position,
			Vector2ic size,
			List<Component> content,
			@Nullable TooltipComponent tooltipImage,
			List<ClientTooltipComponent> components,
			ClientTooltipPositioner positioner,
			int screenWidth,
			int screenHeight
	) {
		this(position, size, content, tooltipImage, components, positioner, new Vector2d());
		offset.set(getPositionerOffset(screenWidth, screenHeight, position.x(), position.y()));
	}

	public boolean isHovering(double mouseX, double mouseY) {
		var x = mouseX + offset.x();
		var y = mouseY + offset.y();
		return x >= position.x() - TOOLTIP_PADDING && x <= position.x() + size.x() + TOOLTIP_PADDING
				&& y >= position.y() - TOOLTIP_PADDING && y <= position.y() + size.y() + TOOLTIP_PADDING;
	}

	public Vector2d getPositionerOffset(int screenWidth, int screenHeight, double x, double y) {
		var offset = positioner.positionTooltip(screenWidth, screenHeight, (int) x, (int) y, size.x(), size.y());
		var deltaX = x - offset.x();
		var deltaY = y - offset.y();
		return new Vector2d(deltaX, deltaY);
	}

	public void setPosition(int screenWidth, int screenHeight, double x, double y) {
		offset.set(getPositionerOffset(screenWidth, screenHeight, x, y));
		var actualX = Math.max(Math.min(x, screenWidth - size.x() + offset.x - TOOLTIP_PADDING), offset.x + TOOLTIP_PADDING);
		var actualY = Math.max(Math.min(y, screenHeight - size.y() + offset.y - TOOLTIP_PADDING), offset.y + TOOLTIP_PADDING);
		position.set(actualX, actualY);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PinnedTooltip that)) {
			return false;
		}
		return Objects.equal(content, that.content) && Objects.equal(
				tooltipImage,
				that.tooltipImage
		);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(content, tooltipImage);
	}
}
