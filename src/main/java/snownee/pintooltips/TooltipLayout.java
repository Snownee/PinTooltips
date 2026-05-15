package snownee.pintooltips;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Style;

public interface TooltipLayout {
	int TOOLTIP_PADDING = 3;

	void updateSize(PinnedTooltip tooltip, int screenWidth, int screenHeight, Font font);

	default @Nullable Style getExtraStyleAt(PinnedTooltip tooltip, double mouseX, double mouseY, Font font) {
		return null;
	}

	default boolean isHovering(PinnedTooltip tooltip, double mouseX, double mouseY) {
		var position = tooltip.position();
		var size = tooltip.size();
		return mouseX >= position.x() - TOOLTIP_PADDING && mouseX <= position.x() + size.x() + TOOLTIP_PADDING &&
				mouseY >= position.y() - TOOLTIP_PADDING && mouseY <= position.y() + size.y() + TOOLTIP_PADDING;
	}

	default void visitLines(PinnedTooltip tooltip, ActiveTextCollector output) {
		ActiveTextCollector.Parameters parameters = output.defaultParameters().withOpacity(0);
		for (Map.Entry<Rect2i, ClientTooltipComponent> entry : tooltip.linesPosition().entrySet()) {
			if (entry.getValue() instanceof ClientTextTooltip text) {
				output.accept(
						TextAlignment.LEFT,
						(int) tooltip.position().x() + entry.getKey().getX(),
						(int) tooltip.position().y() + entry.getKey().getY(),
						parameters,
						text.text);
			}
		}
	}
}
