package snownee.pintooltips.mixin.pin;

import java.util.List;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinnedTooltipsService;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = GuiGraphics.class, priority = 499)
public class GuiGraphicsMixin implements PTGuiGraphics {
	@Shadow
	@Final
	private PoseStack pose;
	@Unique
	private ItemStack pin_tooltips$renderingItemStack = ItemStack.EMPTY;
	@Unique
	private boolean pin_tooltips$renderingPinned = false;
	@Unique
	private boolean pin_tooltips$renderingPinnedEvent = false;

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void pin_tooltips$renderTooltip$recordContext(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		pin_tooltips$setRenderingItemStack(itemStack);
	}

	@Inject(
			method = "renderTooltipInternal",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V")
	)
	private void pin_tooltips$onRender(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final CallbackInfo ci,
			@Local Vector2ic position
	) {
		if (pin_tooltips$renderingPinned) {
			return;
		}
		PinTooltips.onRenderTooltip(
				font,
				components,
				position,
				pin_tooltips$getRenderingItemStack());
	}

	@Inject(
			method = "renderTooltipInternal", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawManaged(Ljava/lang/Runnable;)V"))
	private void pin_tooltips$changeZOffset(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final CallbackInfo ci
	) {
		// Render the unpinned tooltip on top of the pinned tooltip
		if (!pin_tooltips$renderingPinned && !PinnedTooltipsService.INSTANCE.tooltips.isEmpty() || pin_tooltips$renderingPinnedEvent) {
			pose.translate(0, 0, PinTooltips.getMaxZOffset());
		}
	}

	@WrapMethod(method = "renderTooltipInternal")
	private void pin_tooltips$avoidRenderWhenOperating(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final Operation<Void> original) {
		if (PinnedTooltipsService.INSTANCE.hovered == null || pin_tooltips$renderingPinned || pin_tooltips$renderingPinnedEvent) {
			original.call(font, components, mouseX, mouseY, tooltipPositioner);
		}
		pin_tooltips$setRenderingItemStack(ItemStack.EMPTY);
	}

	@Override
	public void pin_tooltips$setRenderingItemStack(ItemStack itemStack) {
		pin_tooltips$renderingItemStack = itemStack;
	}

	@Override
	public ItemStack pin_tooltips$getRenderingItemStack() {
		return pin_tooltips$renderingItemStack;
	}

	@Override
	public void pin_tooltips$setRenderingPinned(boolean value) {
		pin_tooltips$renderingPinned = value;
	}

	@Override
	public void pin_tooltips$setRenderingPinnedEvent(boolean value) {
		pin_tooltips$renderingPinnedEvent = value;
	}
}
