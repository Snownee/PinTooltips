package snownee.pintooltips.mixin.pin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.duck.PTContainerScreen;

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

	@ModifyReceiver(
			method = "renderTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;getTooltipImage()Ljava/util/Optional;"))
	private ItemStack pin_tooltips$recordContext(final ItemStack itemStack) {
		PinTooltipsHooks.renderingItemStack = itemStack;
		return itemStack;
	}
}
