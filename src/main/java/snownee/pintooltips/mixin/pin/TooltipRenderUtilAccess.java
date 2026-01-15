package snownee.pintooltips.mixin.pin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;

@Mixin(TooltipRenderUtil.class)
public interface TooltipRenderUtilAccess {
	@Invoker
	static void callRenderFrameGradient(GuiGraphics graphics, int x, int y, int width, int height, int z, int topColor, int bottomColor) {
		throw new AssertionError();
	}
}
