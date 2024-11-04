package snownee.pintooltips.mixin.farmersdelight;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.ComponentDecorator;
import vectorwing.farmersdelight.client.event.TooltipEvents;

@Mixin(TooltipEvents.class)
public class TooltipEventsMixin {
	@WrapOperation(method = "addTooltipToVanillaSoups", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private static boolean pin_tooltips$addTooltipToVanillaSoups(
			List<Component> tooltip,
			Object object,
			Operation<Boolean> original,
			@Local MobEffectInstance effectInstance) {
		if (PinTooltipsHooks.isGrabbing() && object instanceof MutableComponent component) {
			ComponentDecorator.mobEffect(component, effectInstance);
		}
		return original.call(tooltip, object);
	}
}
