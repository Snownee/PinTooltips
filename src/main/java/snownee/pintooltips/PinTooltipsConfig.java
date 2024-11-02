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
import snownee.pintooltips.util.JsonConfig;

public record PinTooltipsConfig(
		Set<String> screenBlacklist
) {
	public static final Codec<PinTooltipsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf()
					.<Set<String>>xmap(it -> new ObjectOpenHashSet<>(it), List::copyOf)
					.fieldOf("screenBlacklist")
					.forGetter(it -> it.screenBlacklist)
	).apply(instance, PinTooltipsConfig::new));

	public static final JsonConfig<PinTooltipsConfig> INSTANCE;

	static {
		INSTANCE = new JsonConfig<>(
				PinTooltips.configDirectory.toPath().resolve("pin_tooltips.json"),
				CODEC,
				null,
				() -> new PinTooltipsConfig(Set.of(
						PauseScreen.class.getName(),
						ChatScreen.class.getName(),
						GenericDirtMessageScreen.class.getName(),
						ReceivingLevelScreen.class.getName(),
						ProgressScreen.class.getName()
				))
		);
	}
}
