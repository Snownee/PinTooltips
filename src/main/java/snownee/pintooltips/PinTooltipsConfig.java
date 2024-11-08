package snownee.pintooltips;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import snownee.pintooltips.util.DefaultDescriptions;
import snownee.pintooltips.util.JsonConfig;

public record PinTooltipsConfig(
		boolean hideMissingDescriptions,
		boolean jadeModEnchantmentModName,
		boolean jadeModMobEffectModName,
		Set<String> screenBlacklist
) {
	public static final Codec<PinTooltipsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("hideMissingDescriptions")
					.orElse(true)
					.forGetter(PinTooltipsConfig::hideMissingDescriptions),
			Codec.BOOL.fieldOf("jadeModEnchantmentModName")
					.orElse(true)
					.forGetter(PinTooltipsConfig::jadeModEnchantmentModName),
			Codec.BOOL.fieldOf("jadeModMobEffectModName")
					.orElse(true)
					.forGetter(PinTooltipsConfig::jadeModMobEffectModName),
			Codec.STRING.listOf()
					.<Set<String>>xmap(it -> new ObjectOpenHashSet<>(it), List::copyOf)
					.fieldOf("screenBlacklist")
					.orElseGet(PinTooltipsConfig::defaultBlacklist)
					.forGetter(PinTooltipsConfig::screenBlacklist)
	).apply(instance, PinTooltipsConfig::new));

	private static final JsonConfig<PinTooltipsConfig> INSTANCE;

	static {
		INSTANCE = new JsonConfig<>(
				PinTooltips.configDirectory.toPath().resolve("pin_tooltips.json"),
				CODEC,
				DefaultDescriptions::clearCache,
				() -> new PinTooltipsConfig(true, true, true, defaultBlacklist())
		);
	}

	public static PinTooltipsConfig get() {
		return INSTANCE.get();
	}

	private static Set<String> defaultBlacklist() {
		return Set.of(
				PauseScreen.class.getName(),
				ChatScreen.class.getName(),
				GenericDirtMessageScreen.class.getName(),
				ReceivingLevelScreen.class.getName(),
				ProgressScreen.class.getName()
		);
	}
}
