package snownee.pintooltips.mixin.compat;

import java.awt.*;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.obscuria.tooltips.client.renderer.TooltipContext;
import com.obscuria.tooltips.client.renderer.TooltipRenderer;
import com.obscuria.tooltips.client.style.TooltipStyle;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.compat.ObscureTooltipStyle;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = TooltipRenderer.class, remap = false)
public class ObscureTooltipMixin {
	@Shadow
	private static @Nullable TooltipStyle renderStyle;
	@Shadow
	private static ItemStack renderStack;
	@Unique
	private static long pin_tooltips$gameStartMillis = System.currentTimeMillis();
	@Unique
	private static boolean pin_tooltips$renderingPinned;

	@Inject(
			method = "render",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, remap = true))
	private static void pin_tooltips$render(
			TooltipContext renderer,
			ItemStack stack,
			Font font,
			List<ClientTooltipComponent> components,
			int x,
			int y,
			ClientTooltipPositioner positioner,
			CallbackInfoReturnable<Boolean> cir,
			@Local Vector2ic position,
			@Local Point size) {
		PTGuiGraphics graphics = PTGuiGraphics.of(renderer.context());
		if (graphics.pin_tooltips$getRenderingPinned()) {
			renderer.define(stack, 1F + (System.currentTimeMillis() - pin_tooltips$gameStartMillis) / 1000F);
			return;
		}
		PinTooltips.onRenderTooltip(
				font,
				components,
				position,
				graphics.pin_tooltips$getRenderingItemStack(),
				new ObscureTooltipStyle(new Vector2i(size.x, size.y)));
	}

	@WrapMethod(method = "render")
	private static boolean pin_tooltips$wrapRender(
			TooltipContext renderer,
			ItemStack stack,
			Font font,
			List<ClientTooltipComponent> components,
			int x,
			int y,
			ClientTooltipPositioner positioner,
			Operation<Boolean> original) {
		pin_tooltips$renderingPinned = PTGuiGraphics.of(renderer.context()).pin_tooltips$getRenderingPinned();
		ItemStack oStack = renderStack;
		boolean ret = original.call(renderer, stack, font, components, x, y, positioner);
		if (pin_tooltips$renderingPinned) {
			renderStack = oStack;
		}
		return ret;
	}

	@Inject(method = "reset", at = @At("HEAD"), cancellable = true)
	private static void pin_tooltips$reset(CallbackInfo ci) {
		if (pin_tooltips$renderingPinned) {
			if (renderStyle != null) {
				renderStyle.reset();
			}

			renderStyle = null;
			ci.cancel();
		}
	}
}
