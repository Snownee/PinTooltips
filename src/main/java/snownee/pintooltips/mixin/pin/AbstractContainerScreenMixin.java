package snownee.pintooltips.mixin.pin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

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
		if (pin_tooltips$originalHoveredSlot == null) {
			pin_tooltips$originalHoveredSlot = hoveredSlot;
		}
		hoveredSlot = slot;
	}

	@Override
	public void pin_tooltips$dropDummyHoveredSlot() {
		hoveredSlot = pin_tooltips$originalHoveredSlot;
		pin_tooltips$originalHoveredSlot = null;
	}

	@ModifyReceiver(
			method = "renderTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;getTooltipImage()Ljava/util/Optional;"))
	private ItemStack pin_tooltips$recordContext(final ItemStack itemStack, GuiGraphics guiGraphics) {
		PTGuiGraphics.of(guiGraphics).pin_tooltips$setRenderingItemStack(itemStack);
		return itemStack;
	}
}
