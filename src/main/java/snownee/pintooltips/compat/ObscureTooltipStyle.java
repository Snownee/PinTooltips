package snownee.pintooltips.compat;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import snownee.pintooltips.PinnedTooltip;
import snownee.pintooltips.TooltipStyle;

public class ObscureTooltipStyle implements TooltipStyle {
	private final Vector2ic size;

	public ObscureTooltipStyle(Vector2ic size) {
		this.size = size;
	}

	@Override
	public void updateSize(PinnedTooltip tooltip, int screenWidth, int screenHeight, Font font) {
		tooltip.setSize(size.x(), size.y());
	}

	@Override
	public @Nullable Style getStyleAt(PinnedTooltip tooltip, double mouseX, double mouseY, Font font) {
		return null;
	}
}
