package snownee.pintooltips.mixin.pin;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltipsService;
import snownee.pintooltips.PinnedTooltip;
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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@WrapOperation(
			method = "renderTooltip",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
	private void pin_tooltips$grabItem(
			GuiGraphics graphics,
			Font font,
			List<Component> texts,
			Optional<TooltipComponent> optionalImage,
			int xo,
			int yo,
			@Nullable Identifier style,
			Operation<Void> original,
			@Local(name = "item") ItemStack item) {
		PinnedTooltip tooltip = PinTooltipsService.INSTANCE.autoPinnedTooltip();
		if (tooltip != null && tooltip.itemReference() == item) {
			return;
		}
		PTGuiGraphics.of(graphics).pin_tooltips$setRenderingItemStack(item);
		original.call(graphics, font, texts, optionalImage, xo, yo, style);
	}
}
