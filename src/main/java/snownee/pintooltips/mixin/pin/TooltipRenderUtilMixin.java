package snownee.pintooltips.mixin.pin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import snownee.pintooltips.PinTooltips;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
	@Inject(method = "renderFrameGradient", at = @At("TAIL"))
	private static void pin_tooltips$renderFrameGradient(
			GuiGraphics graphics,
			int x,
			int y,
			int width,
			int height,
			int z,
			int topColor,
			int bottomColor,
			CallbackInfo ci) {
		PinTooltips.onRenderFrame(graphics, x, y, width, height, z);
	}
}
