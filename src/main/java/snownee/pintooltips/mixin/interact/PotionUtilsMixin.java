package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.ComponentDecorator;

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
			@Local MobEffectInstance effectInstance) {
		if (PinTooltipsHooks.isGrabbing()) {
			ComponentDecorator.mobEffect(component, effectInstance);
		}
		return component;
	}
}
