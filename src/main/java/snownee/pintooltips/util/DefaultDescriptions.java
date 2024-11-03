package snownee.pintooltips.util;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltipsConfig;

public class DefaultDescriptions {
	private static final Map<Enchantment, Component> ENCHANTMENT_CACHE = new HashMap<>();
	private static final Map<MobEffect, Component> EFFECT_CACHE = new HashMap<>();

	private DefaultDescriptions() {}

	/**
	 * Get the raw, unformatted, translated form of {@code enchantment}'s description (as provided through
	 * {@code <enchantment translation key>.desc}) for {@code enchantment}, or {@code null} if no description
	 * is provided by any language file and {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forEnchantmentRaw(Enchantment enchantment) {
		var translationKey = enchantment.getDescriptionId() + ".desc";
		if (PinTooltipsConfig.get().hideMissingDescriptions() && !Language.getInstance().has(translationKey)) {
			return null;
		}

		return Component.translatable(translationKey);
	}

	/**
	 * Get the default description (as provided by {@link #forEnchantmentRaw(Enchantment)}) for
	 * {@code enchantment}, or {@code null} if no description is provided by any language file and
	 * {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forEnchantmentFormatted(Enchantment enchantment) {
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

		if (PinTooltipsConfig.get().hideMissingDescriptions()) {
			return null;
		}

		return Component.translatable(primaryTranslationKey);
	}

	/**
	 * Get the default description (as provided by {@link #forStatusEffectRaw(StatusEffect)}) for
	 * {@code effect}, or {@code null} if no description is provided by any language file and
	 * {@link IdwtialsimmoedmConfig#hideMissingDescriptions} is {@code true}
	 */
	public static @Nullable Component forStatusEffectFormatted(MobEffect effect) {
		return EFFECT_CACHE.computeIfAbsent(effect, DefaultDescriptions::forStatusEffectRaw);
	}

	public static void clearCache() {
		ENCHANTMENT_CACHE.clear();
		EFFECT_CACHE.clear();
	}
}
