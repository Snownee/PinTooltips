package snownee.pintooltips;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.mixin.compat.ForgeGuiGraphicsAccess;

public class PinTooltipsClient {
	public static void setRenderingItemStack(GuiGraphics graphics, ItemStack itemStack) {
		((ForgeGuiGraphicsAccess) graphics).setTooltipStack(itemStack);
	}
}
