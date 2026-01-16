package snownee.pintooltips.util;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsConfig;

@SuppressWarnings("JavadocReference")
public class DefaultDescriptions {
	private static final Map<Holder<Enchantment>, Component> ENCHANTMENT_CACHE = new HashMap<>();
	private static final Map<MobEffect, Component> EFFECT_CACHE = new HashMap<>();

	private DefaultDescriptions() {}

	/**
	 * Get the raw, unformatted, translated form of {@code enchantment}'s description (as provided through
	 * {@code <enchantment translation key>.desc}) for {@code enchantment}, or {@code null} if no description
	 * is provided by any language file and {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forEnchantmentRaw(Holder<Enchantment> enchantment) {
		var translationKey = Util.makeDescriptionId("enchantment", enchantment.unwrapKey().orElseThrow().identifier()) + ".desc";
		if (PinTooltipsConfig.hideMissingDescriptions && !Language.getInstance().has(translationKey)) {
			return null;
		}

		return clickCopyTranslationKey(translationKey);
	}

	/**
	 * Get the default description (as provided by {@link #forEnchantmentRaw(Enchantment)}) for
	 * {@code enchantment}, or {@code null} if no description is provided by any language file and
	 * {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forEnchantmentFormatted(Holder<Enchantment> enchantment) {
		return ENCHANTMENT_CACHE.computeIfAbsent(enchantment, DefaultDescriptions::forEnchantmentRaw);
	}

	/**
	 * Get the raw, unformatted, translated form of {@code effect}'s description (as provided through
	 * {@code <effect translation key>.desc}) for {@code enchantment}, or {@code null} if no description
	 * is provided by any language file and {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forStatusEffectRaw(MobEffect effect) {
		var primaryTranslationKey = effect.getDescriptionId() + ".desc";
		var secondaryTranslationKey = effect.getDescriptionId() + ".description";

		if (Language.getInstance().has(primaryTranslationKey)) {
			return Component.translatable(primaryTranslationKey);
		}
		if (Language.getInstance().has(secondaryTranslationKey)) {
			return Component.translatable(secondaryTranslationKey);
		}

		if (PinTooltipsConfig.hideMissingDescriptions) {
			return null;
		}

		return clickCopyTranslationKey(primaryTranslationKey);
	}

	/**
	 * Get the default description (as provided by {@link #forStatusEffectRaw(StatusEffect)}) for
	 * {@code effect}, or {@code null} if no description is provided by any language file and
	 * {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forStatusEffectFormatted(MobEffect effect) {
		return EFFECT_CACHE.computeIfAbsent(effect, DefaultDescriptions::forStatusEffectRaw);
	}

	public static Component clickCopyTranslationKey(String key) {
		return Component.translatable(key).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(key)));
	}

	public static void clearCache() {
		ENCHANTMENT_CACHE.clear();
		EFFECT_CACHE.clear();
	}
}
