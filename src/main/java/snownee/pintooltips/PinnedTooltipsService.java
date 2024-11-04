package snownee.pintooltips;

import java.util.LinkedHashSet;

public class PinnedTooltipsService {
	public static final PinnedTooltipsService INSTANCE = new PinnedTooltipsService();

	public final LinkedHashSet<PinnedTooltip> tooltips = new LinkedHashSet<>();

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
		for (var tooltip : tooltips.reversed()) {
			if (tooltip.isHovering(mouseX, mouseY)) {
				return tooltip;
			}
		}
		return null;
	}

	public void clearStates() {
		focused = null;
		hovered = null;
		dragging = false;
		storedDragX = 0;
		storedDragY = 0;
	}
}
