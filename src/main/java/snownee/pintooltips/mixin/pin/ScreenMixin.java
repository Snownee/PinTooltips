package snownee.pintooltips.mixin.pin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import snownee.pintooltips.PinnedTooltipsService;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow
	public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

	/**
	 * Don't render tooltip from the others when operation
	 */
	@WrapMethod(method = "renderWithTooltip")
	private void preventRenderingTooltip(
			final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick, final Operation<Void> original) {
		if (PinnedTooltipsService.INSTANCE.operating) {
			render(guiGraphics, -9999, -9999, partialTick);
		} else {
			original.call(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
}
