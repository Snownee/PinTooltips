package snownee.pintooltips.util;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsCompats;

public class ComponentDecorator {
	public static final Identifier SHOW_EFFECT = PinTooltips.id("show_effect");
	public static final Identifier SHOW_ENCHANTMENT = PinTooltips.id("show_enchantment");

	public static void mobEffect(MutableComponent component, MobEffectInstance effectInstance) {
		Component desc = DefaultDescriptions.forStatusEffectFormatted(effectInstance.getEffect().value());
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, effectInstance);
		HoverEvent hoverEvent = new HoverEvent.ShowText(desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEffect(effectInstance)) {
			MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, effectInstance).ifSuccess(tag -> {
				component.withStyle($ -> $.withClickEvent(new ClickEvent.Custom(SHOW_EFFECT, Optional.of(tag))));
			});
		}
	}

	public static void enchantment(MutableComponent component, Holder<Enchantment> enchantment, int level) {
		var desc = DefaultDescriptions.forEnchantmentFormatted(enchantment);
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, enchantment);
		HoverEvent hoverEvent = new HoverEvent.ShowText(desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEnchantment(enchantment)) {
			CompoundTag tag = new CompoundTag();
			tag.putString("enchantment", enchantment.unwrapKey().orElseThrow().identifier().toString());
			tag.putInt("level", level);
			component.withStyle($ -> $.withClickEvent(new ClickEvent.Custom(SHOW_ENCHANTMENT, Optional.of(tag))));
		}
	}
}
