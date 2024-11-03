package snownee.pintooltips.mixin.interact;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsHooks;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	@Nullable
	public abstract CompoundTag getTag();

	@Shadow
	@Nullable
	private CompoundTag tag;

	@Shadow
	public abstract Item getItem();

	@WrapMethod(method = "getTooltipLines")
	private List<Component> pin_tooltips$handleGrabbing(
			final @Nullable Player player,
			final TooltipFlag isAdvanced,
			final Operation<List<Component>> original) {
		PinTooltipsHooks.markGrabbing();
		var result = original.call(player, isAdvanced);
		PinTooltipsHooks.unmarkGrabbing();
		return result;
	}

	@WrapMethod(method = "getHoverName")
	private Component pin_tooltips$handleHoverName(Operation<Component> original) {
		Component component = original.call();
		if (PinTooltipsHooks.isGrabbing()) {
			MutableComponent mutableComponent;
			if (component instanceof MutableComponent) {
				mutableComponent = (MutableComponent) component;
			} else {
				mutableComponent = component.copy();
			}
			return mutableComponent.withStyle($ -> $.withHoverEvent(PinTooltips.CLICK_TO_COPY_EVENT)
					.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, component.getString())));
		}
		return component;
	}

	@WrapOperation(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
	private MutableComponent pin_tooltips$handleTranslatable(String key, Object[] args, Operation<MutableComponent> original) {
		MutableComponent component = original.call(key, args);
		if (PinTooltipsHooks.isGrabbing() && "item.nbt_tags".equals(key) && PinTooltipsHooks.isGrabbing()) {
			Component prettyComponent = NbtUtils.toPrettyComponent(getTag());
			component.withStyle($ -> $.withHoverEvent(new HoverEvent(
							HoverEvent.Action.SHOW_TEXT,
							prettyComponent.copy().append("\n").append(PinTooltips.CLICK_TO_COPY)))
					.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, prettyComponent.getString())));
		}
		return component;
	}

	@WrapOperation(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
	private MutableComponent pin_tooltips$handleLiteral(String text, Operation<MutableComponent> original) {
		MutableComponent component = original.call(text);
		if (PinTooltipsHooks.isGrabbing() && !text.startsWith("#") && text.equals(BuiltInRegistries.ITEM.getKey(getItem()).toString())) {
			component.withStyle($ -> $.withHoverEvent(PinTooltips.CLICK_TO_COPY_EVENT)
					.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)));
		}
		return component;
	}
}
