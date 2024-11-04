package snownee.pintooltips.mixin.pin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = GuiGraphics.class, priority = 499)
public class GuiGraphicsMixin implements PTGuiGraphics {
	@Unique
	private ItemStack pin_tooltips$renderingItemStack = ItemStack.EMPTY;
	@Unique
	private boolean pin_tooltips$renderingPinnedTooltip = false;

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void pin_tooltips$renderTooltip$recordContext(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		pin_tooltips$setRenderingItemStack(itemStack);
	}

	@Inject(
			method = "renderTooltipInternal",
			at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/util/List;size()I")
	)
	private void pin_tooltips$onRender(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final CallbackInfo ci
	) {
		if (pin_tooltips$renderingPinnedTooltip) {
			return;
		}
		PinTooltips.onRenderTooltip(
				font,
				components,
				mouseX,
				mouseY,
				pin_tooltips$getRenderingItemStack());
	}

	@WrapMethod(method = "renderTooltipInternal")
	private void pin_tooltips$avoidRenderWhenOperating(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final Operation<Void> original) {
		original.call(font, components, mouseX, mouseY, tooltipPositioner);
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
	public void pin_tooltips$setRenderingPinnedTooltip(boolean bl) {
		pin_tooltips$renderingPinnedTooltip = bl;
	}
}
