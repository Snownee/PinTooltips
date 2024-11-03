package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import snownee.pintooltips.PinTooltipsCompats;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.DefaultDescriptions;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {
	@ModifyReceiver(
			method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V",
			at = @At(
					value = "INVOKE",
					ordinal = 0,
					target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"))
	private static MutableComponent addPotionTooltip(
			final MutableComponent component,
			final ChatFormatting format,
			@Local MobEffectInstance effectInstance,
			@Local MobEffect mobEffect) {
		if (PinTooltipsHooks.isGrabbing()) {
			Component desc = DefaultDescriptions.forStatusEffectFormatted(mobEffect);
			if (desc == null) {
				return component;
			}
			component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
			if (PinTooltipsCompats.canClickEffect(effectInstance)) {
				CompoundTag tag = effectInstance.save(new CompoundTag());
				component.withStyle($ -> $.withUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "@pin_tooltips click_effect %s".formatted(tag))));
			}
		}
		return component;
	}
}
