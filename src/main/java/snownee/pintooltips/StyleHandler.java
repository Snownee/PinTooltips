package snownee.pintooltips;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;

public interface StyleHandler<T extends ClientTooltipComponent> {
	@Nullable Style handle(PinnedTooltip tooltip, T component, Font font, int x, int y, int w);
}
