package snownee.pintooltips.duck;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public interface PTGuiGraphics {
	void pin_tooltips$setRenderingItemStack(ItemStack itemStack);

	void pin_tooltips$setRenderingImage(@Nullable ClientTooltipComponent image);

	default void pin_tooltips$clearRenderingItemStack() {
		pin_tooltips$setRenderingItemStack(ItemStack.EMPTY);
		pin_tooltips$setRenderingImage(null);
	}

	ItemStack pin_tooltips$getRenderingItemStack();

	@Nullable ClientTooltipComponent pin_tooltips$getRenderingImage();

	void pin_tooltips$setRenderingPinned(boolean value);

	boolean pin_tooltips$getRenderingPinned();

	void pin_tooltips$setRenderingPinnedEvent(boolean value);

	boolean pin_tooltips$getRenderingPinnedEvent();

	static PTGuiGraphics of(GuiGraphics guiGraphics) {
		return (PTGuiGraphics) guiGraphics;
	}
}
