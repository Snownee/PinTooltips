package snownee.pintooltips.util;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsCompats;

public class ComponentDecorator {
	public static void mobEffect(MutableComponent component, MobEffectInstance effectInstance) {
		Component desc = DefaultDescriptions.forStatusEffectFormatted(effectInstance.getEffect().value());
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, effectInstance);
		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEffect(effectInstance)) {
			var tag = effectInstance.save();
			component.withStyle($ -> $.withClickEvent(new ClickEvent(
					ClickEvent.Action.RUN_COMMAND,
					"@pin_tooltips click_effect %s".formatted(tag))));
		}
	}

	public static void enchantment(MutableComponent component, Holder<Enchantment> enchantment, int level) {
		var desc = DefaultDescriptions.forEnchantmentFormatted(enchantment);
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, enchantment);
		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEnchantment(enchantment)) {
			component.withStyle($ -> $.withClickEvent(new ClickEvent(
					ClickEvent.Action.RUN_COMMAND,
					"@pin_tooltips click_enchantment %s %d".formatted(enchantment.unwrapKey().orElseThrow().location(), level))));
		}
	}
}
