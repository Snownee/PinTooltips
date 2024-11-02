package snownee.pintooltips.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.duck.PTContainerScreen;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = AbstractContainerScreen.class, priority = 499)
public class AbstractContainerScreenMixin implements PTContainerScreen {
	@Shadow
	@Nullable
	protected Slot hoveredSlot;
	@Unique
	@Nullable
	private Slot pin_tooltips$originalHoveredSlot;

	@Override
	public void pin_tooltips$setDummyHoveredSlot(@Nullable Slot slot) {
		if (slot == null) {
			if (pin_tooltips$originalHoveredSlot != null) {
				hoveredSlot = pin_tooltips$originalHoveredSlot;
			}
		} else {
			pin_tooltips$originalHoveredSlot = hoveredSlot;
			hoveredSlot = slot;
		}
	}

	@Inject(
			method = "renderTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderItemTooltipPre(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci, ItemStack itemStack) {
		((PTGuiGraphics) guiGraphics).pin_tooltips$setRenderingTooltipItemStack(itemStack);
	}

	@Inject(
			method = "renderTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
					shift = At.Shift.AFTER))
	private void renderItemTooltipPost(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
		((PTGuiGraphics) guiGraphics).pin_tooltips$setRenderingTooltipItemStack(ItemStack.EMPTY);
	}
}
