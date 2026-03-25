package snownee.pintooltips.mixin.pin;

import java.util.List;
import java.util.Optional;

import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsClient;
import snownee.pintooltips.PinTooltipsService;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(value = GuiGraphicsExtractor.class, priority = 499)
public class GuiGraphicsMixin implements PTGuiGraphics {
	@Shadow
	private @Nullable Runnable deferredTooltip;
	@Unique
	private ItemStack pin_tooltips$renderingItemStack = ItemStack.EMPTY;
	@Unique
	private boolean pin_tooltips$renderingPinned = false;
	@Unique
	private boolean pin_tooltips$renderingPinnedEvent = false;
	@Unique
	private @Nullable ClientTooltipComponent pin_tooltips$renderingImage;

	@Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void pin_tooltips$renderTooltip$grabItem(Font font, ItemStack itemStack, int xo, int yo, CallbackInfo ci) {
		pin_tooltips$setRenderingItemStack(itemStack);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(
			method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V"))
	private void pin_tooltips$renderTooltip$grabImage(
			Font font,
			List<Component> texts,
			Optional<TooltipComponent> optionalImage,
			int xo,
			int yo,
			@Nullable Identifier style,
			CallbackInfo ci,
			@Local(name = "components") List<ClientTooltipComponent> components) {
		if (optionalImage.isPresent() && !components.isEmpty()) {
			pin_tooltips$setRenderingImage(components.get(components.size() == 1 ? 0 : 1));
		}
	}

	@Inject(
			method = "renderTooltip",
			at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;")
	)
	private void pin_tooltips$onRender(
			Font font,
			List<ClientTooltipComponent> lines,
			int xo,
			int yo,
			ClientTooltipPositioner positioner,
			@Nullable Identifier style,
			CallbackInfo ci,
			@Local(name = "positionedTooltip") Vector2ic positionedTooltip
	) {
		PinTooltips.onRenderTooltip((GuiGraphicsExtractor) (Object) this, font, lines, style, positionedTooltip, null);
	}

	@WrapMethod(method = "setTooltipForNextFrameInternal")
	private void pin_tooltips$avoidRenderWhenOperating(
			Font font,
			List<ClientTooltipComponent> lines,
			int xo,
			int yo,
			ClientTooltipPositioner positioner,
			@Nullable Identifier style,
			boolean replaceExisting,
			Operation<Void> original) {
		if (PinTooltipsService.INSTANCE.hovered == null || pin_tooltips$renderingPinned || pin_tooltips$renderingPinnedEvent) {
			original.call(font, lines, xo, yo, positioner, style, replaceExisting);
		}
		pin_tooltips$clearRenderingItemStack();
	}

	@WrapOperation(
			method = "setTooltipForNextFrameInternal",
			at = @At(
					value = "FIELD",
					opcode = Opcodes.PUTFIELD,
					target = "Lnet/minecraft/client/gui/GuiGraphics;deferredTooltip:Ljava/lang/Runnable;"))
	private void pin_tooltips$grabContextInternal(GuiGraphicsExtractor graphics, Runnable value, Operation<Void> original) {
		original.call(graphics, value);
		Runnable runnable = deferredTooltip;
		ItemStack itemStack = pin_tooltips$getRenderingItemStack();
		ClientTooltipComponent image = pin_tooltips$getRenderingImage();
		if (runnable != null && !itemStack.isEmpty()) {
			deferredTooltip = () -> {
				pin_tooltips$setRenderingItemStack(itemStack);
				pin_tooltips$setRenderingImage(image);
				runnable.run();
				pin_tooltips$clearRenderingItemStack();
			};
		}
	}

	@Override
	public void pin_tooltips$setRenderingItemStack(ItemStack itemStack) {
		pin_tooltips$renderingItemStack = itemStack;
		PinTooltipsClient.setRenderingItemStack((GuiGraphicsExtractor) (Object) this, itemStack);
	}

	@Override
	public void pin_tooltips$setRenderingImage(@Nullable ClientTooltipComponent image) {
		if (image == null || !pin_tooltips$renderingItemStack.isEmpty()) {
			pin_tooltips$renderingImage = image;
		}
	}

	@Override
	public ItemStack pin_tooltips$getRenderingItemStack() {
		return pin_tooltips$renderingItemStack;
	}

	@Override
	public @Nullable ClientTooltipComponent pin_tooltips$getRenderingImage() {
		return pin_tooltips$renderingImage;
	}

	@Override
	public void pin_tooltips$setRenderingPinned(boolean value) {
		pin_tooltips$renderingPinned = value;
	}

	@Override
	public boolean pin_tooltips$getRenderingPinned() {
		return pin_tooltips$renderingPinned;
	}

	@Override
	public void pin_tooltips$setRenderingPinnedEvent(boolean value) {
		pin_tooltips$renderingPinnedEvent = value;
	}

	@Override
	public boolean pin_tooltips$getRenderingPinnedEvent() {
		return pin_tooltips$renderingPinnedEvent;
	}
}