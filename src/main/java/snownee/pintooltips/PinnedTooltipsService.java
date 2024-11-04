package snownee.pintooltips;

import java.util.ArrayList;
import java.util.List;

public class PinnedTooltipsService {
	public static final PinnedTooltipsService INSTANCE = new PinnedTooltipsService();

	public final List<PinnedTooltip> tooltips = new ArrayList<>();

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
		return tooltips.stream()
				.filter(it -> it.isHovering(mouseX, mouseY))
				.findFirst()
				.orElse(null);
	}

	public void clearStates() {
		focused = null;
		hovered = null;
		dragging = false;
		storedDragX = 0;
		storedDragY = 0;
	}
}
