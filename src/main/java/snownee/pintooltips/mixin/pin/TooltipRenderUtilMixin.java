package snownee.pintooltips.mixin.pin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import snownee.pintooltips.PinTooltips;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
	@Inject(method = "renderTooltipBackground", at = @At("TAIL"))
	private static void pin_tooltips$renderFrameGradient(
			GuiGraphics graphics,
			int x,
			int y,
			int w,
			int h,
			@Nullable Identifier style,
			CallbackInfo ci) {
		PinTooltips.onRenderFrame(graphics, x, y, w, h, style);
	}
}
