package snownee.pintooltips;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

public interface TooltipStyle {
	int TOOLTIP_PADDING = 3;

	void updateSize(PinnedTooltip tooltip, int screenWidth, int screenHeight, Font font);

	@Nullable Style getStyleAt(PinnedTooltip tooltip, double mouseX, double mouseY, Font font);

	default boolean isHovering(PinnedTooltip tooltip, double mouseX, double mouseY) {
		var position = tooltip.position();
		var size = tooltip.size();
		return mouseX >= position.x() - TOOLTIP_PADDING && mouseX <= position.x() + size.x() + TOOLTIP_PADDING
				&& mouseY >= position.y() - TOOLTIP_PADDING && mouseY <= position.y() + size.y() + TOOLTIP_PADDING;
	}
}
