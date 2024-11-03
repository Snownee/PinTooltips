package snownee.pintooltips;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

public class PinnedTooltipsService {
	public static final PinnedTooltipsService INSTANCE = new PinnedTooltipsService();

	public final Set<PinnedTooltip> tooltips = new ReferenceLinkedOpenHashSet<>();

	public PinnedTooltip focused = null;

	public boolean operating = false;

	private PinnedTooltipsService() {
	}

	public PinnedTooltip findFocused(double mouseX, double mouseY) {
		return tooltips.stream()
				.filter(it -> it.isHovering(mouseX, mouseY))
				.findFirst()
				.orElse(null);
	}
}
