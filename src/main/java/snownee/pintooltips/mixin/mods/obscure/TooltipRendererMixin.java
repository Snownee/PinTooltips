/*
package snownee.pintooltips.mixin.mods.obscure;

import java.util.List;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.obscuria.tooltips.client.TooltipRenderer;
import dev.obscuria.tooltips.client.TooltipState;
import dev.obscuria.tooltips.client.tooltip.layout.TooltipLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(TooltipRenderer.class)
public class TooltipRendererMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0))
	private static void pin_tooltips$onRender(
			GuiGraphics graphics,
			Font font,
			List<ClientTooltipComponent> components,
			int mouseX,
			int mouseY,
			ClientTooltipPositioner positioner,
			CallbackInfoReturnable<Boolean> cir,
			@Local(name = "pos") Vector2ic position) {
		PinTooltips.onRenderTooltip(graphics, font, components, position, null);
	}

	@WrapOperation(
			method = "render", at = @At(
			value = "INVOKE",
			target = "Ldev/obscuria/tooltips/client/tooltip/layout/TooltipLayout;rawProcessPreWrap(Ldev/obscuria/tooltips/client/TooltipState;Ljava/util/List;Lnet/minecraft/client/gui/Font;)Ljava/util/List;"))
	private static List<ClientTooltipComponent> pin_tooltips$skipProcessPreWrap(
			TooltipLayout<?> instance,
			TooltipState state,
			List<ClientTooltipComponent> components,
			Font font,
			Operation<List<ClientTooltipComponent>> original,
			@Local(argsOnly = true) GuiGraphics graphics) {
		if (PTGuiGraphics.of(graphics).pin_tooltips$getRenderingPinned()) {
			return components;
		} else {
			return original.call(instance, state, components, font);
		}
	}

	@WrapOperation(
			method = "render", at = @At(
			value = "INVOKE",
			target = "Ldev/obscuria/tooltips/client/TooltipHelper;wrapLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Lnet/minecraft/client/gui/Font;)Ljava/util/List;"))
	private static List<ClientTooltipComponent> pin_tooltips$skipWrapLines(
			GuiGraphics graphics,
			List<ClientTooltipComponent> components,
			Font font,
			Operation<List<ClientTooltipComponent>> original) {
		if (PTGuiGraphics.of(graphics).pin_tooltips$getRenderingPinned()) {
			return components;
		} else {
			return original.call(graphics, components, font);
		}
	}

	@WrapOperation(
			method = "render", at = @At(
			value = "INVOKE",
			target = "Ldev/obscuria/tooltips/client/tooltip/layout/TooltipLayout;rawProcessPostWrap(Ldev/obscuria/tooltips/client/TooltipState;Ljava/util/List;Lnet/minecraft/client/gui/Font;)Ljava/util/List;"))
	private static List<ClientTooltipComponent> pin_tooltips$skipProcessPostWrap(
			TooltipLayout<?> instance,
			TooltipState state,
			List<ClientTooltipComponent> components,
			Font font,
			Operation<List<ClientTooltipComponent>> original,
			@Local(argsOnly = true) GuiGraphics graphics) {
		if (PTGuiGraphics.of(graphics).pin_tooltips$getRenderingPinned()) {
			return components;
		} else {
			return original.call(instance, state, components, font);
		}
	}

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Ldev/obscuria/tooltips/client/TooltipState;renderFrame(Lnet/minecraft/client/gui/GuiGraphics;Lorg/joml/Vector2ic;II)V"))
	private static void pin_tooltips$renderHoverHighlight(
			TooltipState instance,
			GuiGraphics graphics,
			Vector2ic pos,
			int width,
			int height,
			Operation<Void> original) {
		PinTooltips.onRenderFrame(graphics, pos.x() - 2, pos.y() - 2, width + 4, height + 5, 0);
	}
}
*/
