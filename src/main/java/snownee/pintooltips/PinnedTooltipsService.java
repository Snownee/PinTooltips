package snownee.pintooltips;

import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

public class PinnedTooltipsService {
	public static final PinnedTooltipsService INSTANCE = new PinnedTooltipsService();

	public final List<PinnedTooltip> tooltips = new ReferenceArrayList<>();

	public PinnedTooltip focused;
	public PinnedTooltip hovered;

	public boolean dragging;
	public double storedDragX;
	public double storedDragY;

	private PinnedTooltipsService() {
	}

	public PinnedTooltip findHovered(double mouseX, double mouseY) {
		if (tooltips.isEmpty()) {
			return null;
		}

		for (var tooltip : Lists.reverse(tooltips)) {
			if (tooltip.isHovering(mouseX, mouseY)) {
				return tooltip;
			}
		}
		return null;
	}

	public void clearStates() {
		focused = null;
		dragging = false;
		storedDragX = 0;
		storedDragY = 0;
	}
}
