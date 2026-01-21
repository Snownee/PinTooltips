package snownee.pintooltips;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.joml.Vector2d;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PinTooltipsService {
	public static final PinTooltipsService INSTANCE = new PinTooltipsService();

	private @Nullable PinnedTooltip autoPinnedTooltip;
	private final List<PinnedTooltip> tooltips = Collections.synchronizedList(new ReferenceArrayList<>());

	public @Nullable PinnedTooltip focused;
	public @Nullable PinnedTooltip hovered;

	public boolean dragging;
	public double storedDragX;
	public double storedDragY;

	private PinTooltipsService() {
	}

	public @Nullable PinnedTooltip findHovered(double mouseX, double mouseY) {
		if (tooltips.isEmpty()) {
			return null;
		}

		for (var tooltip : Lists.reverse(tooltips)) {
			if (Objects.requireNonNull(tooltip).isHovering(mouseX, mouseY)) {
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

	public void pin(
			@Nullable TooltipLayout layout,
			Vector2ic position,
			Font font,
			List<ClientTooltipComponent> components,
			@Nullable Identifier style,
			ItemStack itemStack,
			@Nullable ClientTooltipComponent image,
			long autoPinnedTimestamp) {
		if (autoPinnedTimestamp > 0 && autoPinnedTooltip != null && autoPinnedTooltip.autoPinnedTimestamp == autoPinnedTimestamp) {
			return;
		}

		// Avoid modifying the tooltips when rendering the tooltip hover event that will cause crash.
		Minecraft.getInstance().execute(() -> {
			PinnedTooltip tooltip = new PinnedTooltip(
					layout == null ? DefaultTooltipLayout.INSTANCE : layout,
					new Vector2d(position),
					components,
					style,
					Minecraft.getInstance().getWindow().getGuiScaledWidth(),
					Minecraft.getInstance().getWindow().getGuiScaledHeight(),
					font,
					itemStack,
					image,
					autoPinnedTimestamp);
			if (autoPinnedTimestamp > 0) {
				if (autoPinnedTooltip != null) {
					tooltips.remove(autoPinnedTooltip);
				}
				autoPinnedTooltip = tooltip;
			}
			tooltips.add(tooltip);
		});
	}

	public void unpin(PinnedTooltip tooltip) {
		// Avoid modifying the tooltips when rendering the tooltip hover event that will cause crash.
		Minecraft.getInstance().execute(() -> {
			tooltips.remove(tooltip);
			if (autoPinnedTooltip == tooltip) {
				autoPinnedTooltip = null;
			}
		});
	}

	public void placeOnTop(PinnedTooltip tooltip) {
		tooltips.remove(tooltip);
		tooltips.add(tooltip);
	}

	public void clearTooltips() {
		tooltips.clear();
		autoPinnedTooltip = null;
	}

	public List<PinnedTooltip> tooltips() {
		return Collections.unmodifiableList(tooltips);
	}

	public @Nullable PinnedTooltip autoPinnedTooltip() {
		return autoPinnedTooltip;
	}
}
