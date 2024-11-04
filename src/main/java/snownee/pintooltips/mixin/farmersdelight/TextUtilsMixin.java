package snownee.pintooltips.mixin.farmersdelight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.ComponentDecorator;
import vectorwing.farmersdelight.common.utility.TextUtils;

@Mixin(TextUtils.class)
public class TextUtilsMixin {
	@ModifyReceiver(
			method = "addFoodEffectTooltip",
			at = @At(
					value = "INVOKE",
					ordinal = 0,
					target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"))
	private static MutableComponent addFoodEffectTooltip(
			MutableComponent component,
			ChatFormatting format,
			@Local MobEffectInstance effectInstance) {
		if (PinTooltipsHooks.isGrabbing()) {
			ComponentDecorator.mobEffect(component, effectInstance);
		}
		return component;
	}
}
