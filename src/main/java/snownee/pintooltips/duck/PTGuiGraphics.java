package snownee.pintooltips.duck;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface PTGuiGraphics {
	void pin_tooltips$setRenderingItemStack(ItemStack itemStack);

	ItemStack pin_tooltips$getRenderingItemStack();

	void pin_tooltips$setRenderingPinned(boolean value);

	void pin_tooltips$setRenderingPinnedEvent(boolean value);

	static PTGuiGraphics of(GuiGraphics guiGraphics) {
		return (PTGuiGraphics) guiGraphics;
	}
}
