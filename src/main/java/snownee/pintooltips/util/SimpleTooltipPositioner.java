package snownee.pintooltips.util;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

@Environment(EnvType.CLIENT)
public class SimpleTooltipPositioner implements ClientTooltipPositioner {
	public static final SimpleTooltipPositioner INSTANCE = new SimpleTooltipPositioner();
	public static final int TOOLTIP_PADDING = 4;

	@Override
	public @NotNull Vector2ic positionTooltip(
			int screenWidth,
			int screenHeight,
			int mouseX,
			int mouseY,
			int tooltipWidth,
			int tooltipHeight
	) {
		var pos = new Vector2i(mouseX, mouseY).add(12, -12);
		if (pos.x < TOOLTIP_PADDING) {
			pos.x = TOOLTIP_PADDING;
		}
		if (pos.y < TOOLTIP_PADDING) {
			pos.y = TOOLTIP_PADDING;
		}
		if (pos.x + tooltipWidth + TOOLTIP_PADDING > screenWidth) {
			pos.x = screenWidth - tooltipWidth - TOOLTIP_PADDING;
		}
		if (pos.y + tooltipHeight + TOOLTIP_PADDING > screenHeight) {
			pos.y = screenHeight - tooltipHeight - TOOLTIP_PADDING;
		}
		return pos;
	}
}
