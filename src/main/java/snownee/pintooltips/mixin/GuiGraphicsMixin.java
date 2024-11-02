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
	private final ThreadLocal<List<Component>> pin_tooltips$content = new ThreadLocal<>();
	@Unique
	private final ThreadLocal<TooltipComponent> pin_tooltips$tooltipImage = new ThreadLocal<>();
	@Unique
	private ItemStack pin_tooltips$itemStack = ItemStack.EMPTY;

	@Inject(
			method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
			at = @At("HEAD")
	)
	private void renderTooltip$onRender(
			final Font font,
			final List<Component> tooltipLines,
			final Optional<TooltipComponent> visualTooltipComponent,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content.set(tooltipLines);
		pin_tooltips$tooltipImage.set(visualTooltipComponent.orElse(null));
	}

	@Inject(
			method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V",
			at = @At("HEAD")
	)
	private void renderTooltip$onRender(
			final Font font,
			final Component text,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content.set(List.of(text));
	}

	@Inject(
			method = "renderComponentTooltip",
			at = @At("HEAD")
	)
	private void renderComponentTooltip$onRender(
			final Font font,
			final List<Component> tooltipLines,
			final int mouseX,
			final int mouseY,
			final CallbackInfo ci
	) {
		pin_tooltips$content.set(tooltipLines);
	}

	@Inject(
			method = "renderTooltipInternal",
			at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/util/List;size()I")
	)
	private void renderTooltipInternal$onRender(
			final Font font,
			final List<ClientTooltipComponent> components,
			final int mouseX,
			final int mouseY,
			final ClientTooltipPositioner tooltipPositioner,
			final CallbackInfo ci
	) {
		if (pin_tooltips$content.get() == null) {
			return;
		}
		PinTooltips.onRenderTooltip(
				font,
				pin_tooltips$content.get(),
				pin_tooltips$tooltipImage.get(),
				components,
				mouseX,
				mouseY,
				tooltipPositioner,
				pin_tooltips$itemStack);
		pin_tooltips$content.remove();
		pin_tooltips$tooltipImage.remove();
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
