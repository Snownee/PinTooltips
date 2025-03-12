package snownee.pintooltips.mixin.pin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.util.Mth;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsConfig;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(TooltipRenderUtil.class)
public class TooltipRenderUtilMixin {
	@Unique
	private static final ThreadLocal<Boolean> pin_tooltips$lock = ThreadLocal.withInitial(() -> false);

	@Shadow
	private static void renderFrameGradient(
			GuiGraphics p_282000_,
			int p_282055_,
			int p_281580_,
			int p_283284_,
			int p_282599_,
			int p_283432_,
			int p_282907_,
			int p_283153_) {
		throw new AssertionError();
	}

	@Inject(method = "renderFrameGradient", at = @At("TAIL"))
	private static void pin_tooltips$renderFrameGradient(
			GuiGraphics context,
			int p_282055_,
			int p_281580_,
			int p_283284_,
			int p_282599_,
			int zOffset,
			int borderTop,
			int borderBottom,
			CallbackInfo ci) {
		if (PinTooltips.isGrabbing() || PTGuiGraphics.of(context).pin_tooltips$getRenderingPinned()) {
			return;
		}
		if (pin_tooltips$lock.get()) {
			return;
		}
		int delay = PinTooltipsConfig.hoveringAutoPinDelay;
		if (PinTooltips.lastMouseMovedTime == 0 || delay <= 0) {
			return;
		}
		float ratio = Mth.clamp((float) (System.currentTimeMillis() - PinTooltips.lastMouseMovedTime) / delay, 0, 1);
		if (ratio < 0.2F) {
			return;
		}
		int color = 0xFFFFFF | (int) (ratio * 0xFF) << 24;
		pin_tooltips$lock.set(true);
		renderFrameGradient(context, p_282055_, p_281580_, p_283284_, p_282599_, zOffset + 1, color, color);
		pin_tooltips$lock.set(false);
	}
}
