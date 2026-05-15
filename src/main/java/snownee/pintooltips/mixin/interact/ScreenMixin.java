package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.PinTooltipsService;
import snownee.pintooltips.util.ComponentDecorator;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(
			method = "defaultHandleGameClickEvent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"),
			cancellable = true)
	private static void pin_tooltips$defaultHandleGameClickEvent(
			ClickEvent event,
			Minecraft minecraft,
			Screen activeScreen,
			CallbackInfo ci,
			@Local(name = "custom") ClickEvent.Custom custom) {
		Identifier id = custom.id();
		if (!id.getNamespace().equals(PinTooltips.ID)) {
			return;
		}
		ComponentDecorator.handleClickEvent(activeScreen, id, custom.payload().orElse(null));
		ci.cancel();
	}

	@WrapMethod(method = "extractRenderStateWithTooltipAndSubtitles")
	private void pin_tooltips$renderWithTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, Operation<Void> original) {
		boolean grabbing = PinTooltipsHooks.markGrabbing();
		if (PinTooltipsService.INSTANCE.hovered != null) {
			mouseX = -100;
			mouseY = -100;
		}
		original.call(graphics, mouseX, mouseY, a);
		PinTooltipsHooks.unmarkGrabbing(grabbing);
	}
}
