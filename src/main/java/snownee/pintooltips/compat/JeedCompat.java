package snownee.pintooltips.compat;

import net.mehvahdjukaar.jeed.Jeed;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;

public class JeedCompat {
	public static boolean canClickEffect(MobEffectInstance effectInstance) {
		return isAvailable()
				&& !BuiltInRegistries.MOB_EFFECT.getTag(Jeed.HIDDEN)
				.map(it -> it.contains(effectInstance.getEffect()))
				.orElse(false);
	}

	public static void clickEffect(MobEffectInstance effectInstance, double mouseX, double mouseY, int button) {
		if (isAvailable()) {
			Jeed.PLUGIN.onClickedEffect(effectInstance, mouseX, mouseY, button);
		}
	}

	private static boolean isAvailable() {
		return Jeed.PLUGIN != null && Minecraft.getInstance().level != null;
	}
}
