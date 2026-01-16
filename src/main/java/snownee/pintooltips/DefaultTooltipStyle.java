package snownee.pintooltips;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Style;

public class DefaultTooltipStyle implements TooltipStyle {
	public static final DefaultTooltipStyle INSTANCE = new DefaultTooltipStyle();

	@Override
	public void updateSize(PinnedTooltip tooltip, int screenWidth, int screenHeight, Font font) {
		var width = 0;
		var height = 0;
		tooltip.linesPosition().clear();
		for (var component : tooltip.components()) {
			var componentWidth = component.getWidth(font);
			var componentHeight = component.getHeight(font);
			tooltip.linesPosition().put(new Rect2i(0, height, componentWidth, componentHeight), component);
			width = Math.max(width, componentWidth);
			height += componentHeight;
		}
		if (width != tooltip.size().x() || height != tooltip.size().y()) {
			tooltip.setSize(width, height);
		}
	}

	@Override
	public @Nullable Style getStyleAt(PinnedTooltip tooltip, double mouseX, double mouseY, Font font) {
		var relativeX = (int) (mouseX - tooltip.position().x());
		var relativeY = (int) (mouseY - tooltip.position().y());
		var line = tooltip.linesPosition().keySet().stream().filter(rect -> rect.contains(relativeX, relativeY)).findFirst().orElse(null);
		var component = tooltip.linesPosition().get(line);
//		if (component instanceof ClientTextTooltip textTooltip) {
//			return font.getSplitter().componentStyleAtWidth(textTooltip.text, relativeX);
//		}
		return null;
	}
}
