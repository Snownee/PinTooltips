package snownee.pintooltips.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import snownee.pintooltips.PinnedTooltipsService;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow
	public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

	@Inject(
			method = "renderWithTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),
			cancellable = true)
	private void preventRenderingTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (PinnedTooltipsService.INSTANCE.findFocused(mouseX, mouseY) != null) {
			render(guiGraphics, -9999, -9999, partialTick);
			ci.cancel();
		}
	}
}
