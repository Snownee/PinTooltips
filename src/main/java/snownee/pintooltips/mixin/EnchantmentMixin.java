package snownee.pintooltips.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.util.DefaultDescriptions;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	@Inject(method = "getFullname", at = @At("HEAD"))
	private void getFullname(int level, CallbackInfoReturnable<Component> cir) {
		if (PinTooltips.isHoldingKey() && cir.getReturnValue() instanceof MutableComponent component) {
			Component desc = DefaultDescriptions.forEnchantmentFormatted((Enchantment) (Object) this);
			if (desc == null) {
				return;
			}
			component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
		}
	}
}
