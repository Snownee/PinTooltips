package snownee.pintooltips.mixin.neo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import snownee.pintooltips.PinTooltipsService;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void pin_tooltips$render(
			Screen screen,
			GuiGraphicsExtractor guiGraphics,
			int mouseX,
			int mouseY,
			float partialTick,
			Operation<Void> original) {
		if (PinTooltipsService.INSTANCE.hovered != null) {
			mouseX = Integer.MAX_VALUE;
			mouseY = Integer.MAX_VALUE;
		}
		original.call(screen, guiGraphics, mouseX, mouseY, partialTick);
	}
}
