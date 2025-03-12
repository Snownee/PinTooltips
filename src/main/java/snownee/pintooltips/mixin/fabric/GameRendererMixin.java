package snownee.pintooltips.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import snownee.pintooltips.PinnedTooltipsService;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void pin_tooltips$render(
			Screen screen,
			GuiGraphics guiGraphics,
			int mouseX,
			int mouseY,
			float partialTick,
			Operation<Void> original) {
		if (PinnedTooltipsService.INSTANCE.hovered != null) {
			mouseX = Integer.MAX_VALUE;
			mouseY = Integer.MAX_VALUE;
		}
		original.call(screen, guiGraphics, mouseX, mouseY, partialTick);
	}
}
