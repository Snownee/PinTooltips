package snownee.pintooltips;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;

public class DefaultTooltipLayout implements TooltipLayout {
	public static final DefaultTooltipLayout INSTANCE = new DefaultTooltipLayout();

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
		for (var position : tooltip.linesPosition().keySet()) {
			position.setWidth(width);
		}
		if (width != tooltip.size().x() || height != tooltip.size().y()) {
			tooltip.setSize(width, height);
		}
	}
}
