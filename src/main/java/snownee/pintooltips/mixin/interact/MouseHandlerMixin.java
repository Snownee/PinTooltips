package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import snownee.pintooltips.PinTooltips;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@ModifyReceiver(
			method = "method_1602",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(DDIDD)Z")
	)
	private Screen pin_tooltips$onDrag(
			final Screen instance,
			final double mouseX,
			final double mouseY,
			final int button,
			final double deltaX,
			final double deltaY
	) {
		PinTooltips.onDrag(instance, button, deltaX, deltaY);
		return instance;
	}
}
