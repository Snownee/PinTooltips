package snownee.pintooltips.mixin.pin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import snownee.pintooltips.PinTooltips;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(
			method = "extractRenderStateWithTooltipAndSubtitles",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;extractDeferredElements(IIF)V"))
	private void pin_tooltips$renderWithTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		PinTooltips.renderPinnedTooltips((Screen) (Object) this, graphics, mouseX, mouseY);
	}
}
