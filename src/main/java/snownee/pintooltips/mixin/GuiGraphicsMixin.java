package snownee.pintooltips.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = GuiGraphics.class, priority = 499)
public class GuiGraphicsMixin implements PTGuiGraphics {
	@Unique
	private List<Component> pin_tooltips$content = null;
	@Unique
	private TooltipComponent pin_tooltips$tooltipImage = null;
	@Unique
	private ItemStack pin_tooltips$itemStack = ItemStack.EMPTY;

	@Inject(
			method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
			at = @At("HEAD")
	)
	private void pin_tooltips$renderTooltip$onRender(
			final Font font,
			final List<Component> tooltipLines,
			final Optional<TooltipComponent> visualTooltipComponent,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content = tooltipLines;
		pin_tooltips$tooltipImage = visualTooltipComponent.orElse(null);
	}

	@Inject(
			method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V",
			at = @At("HEAD")
	)
	private void pin_tooltips$renderTooltip$onRender(
			final Font font,
			final Component text,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content = List.of(text);
	}

	@Inject(
			method = "renderComponentTooltip",
			at = @At("HEAD")
	)
	private void pin_tooltips$renderComponentTooltip$onRender(
			final Font font,
			final List<Component> tooltipLines,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content = tooltipLines;
	}

	@Inject(
			method = "renderTooltipInternal",
			at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/util/List;size()I")
	)
	private void pin_tooltips$renderTooltipInternal$onRender(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final CallbackInfo ci
	) {
		if (pin_tooltips$content == null) {
			return;
		}
		PinTooltips.onRenderTooltip(
				font,
				pin_tooltips$content,
				pin_tooltips$tooltipImage,
				components,
				mouseX,
				mouseY,
				tooltipPositioner,
				pin_tooltips$itemStack);
		pin_tooltips$content = null;
		pin_tooltips$tooltipImage = null;
	}

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void renderItemTooltipPre(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		pin_tooltips$setRenderingTooltipItemStack(itemStack);
	}

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
	private void renderItemTooltipPost(Font font, ItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		pin_tooltips$setRenderingTooltipItemStack(ItemStack.EMPTY);
	}

	@Override
	public void pin_tooltips$setRenderingTooltipItemStack(ItemStack itemStack) {
		pin_tooltips$itemStack = itemStack;
	}
}
