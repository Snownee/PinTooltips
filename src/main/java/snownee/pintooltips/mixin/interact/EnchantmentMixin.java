package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.util.DefaultDescriptions;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@ModifyReturnValue(method = "getFullname", at = @At("RETURN"))
	private Component getFullname(final Component original, int level) {
		if (PinTooltipsHooks.isGrabbing() && original instanceof MutableComponent component) {
			var desc = DefaultDescriptions.forEnchantmentFormatted((Enchantment) (Object) this);
			if (desc == null) {
				return null;
			}
			component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
		}
		return original;
	}
}
