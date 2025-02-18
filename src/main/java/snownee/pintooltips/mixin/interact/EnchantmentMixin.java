package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.ComponentDecorator;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@ModifyReturnValue(method = "getFullname", at = @At("RETURN"))
	private static Component getFullname(final Component original, Holder<Enchantment> enchantment, int level) {
		if (PinTooltipsHooks.isGrabbing() && original instanceof MutableComponent component) {
			ComponentDecorator.enchantment(component, enchantment, level);
		}
		return original;
	}
}
