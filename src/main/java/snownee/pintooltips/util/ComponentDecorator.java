package snownee.pintooltips.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsCompats;

public class ComponentDecorator {
	public static void mobEffect(MutableComponent component, MobEffectInstance effectInstance) {
		Component desc = DefaultDescriptions.forStatusEffectFormatted(effectInstance.getEffect());
		if (desc == null) {
			return;
		}
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
		if (PinTooltipsCompats.canClickEffect(effectInstance)) {
			CompoundTag tag = effectInstance.save(new CompoundTag());
			component.withStyle($ -> $.withUnderlined(true)
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "@pin_tooltips click_effect %s".formatted(tag))));
		}
	}

	public static void enchantment(MutableComponent component, Enchantment enchantment, int level) {
		var desc = DefaultDescriptions.forEnchantmentFormatted(enchantment);
		if (desc == null) {
			return;
		}
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
	}
}
