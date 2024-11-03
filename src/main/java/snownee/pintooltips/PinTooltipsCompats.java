package snownee.pintooltips;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.effect.MobEffectInstance;
import snownee.pintooltips.compat.JeedCompat;

public class PinTooltipsCompats {
	public static boolean jeed = FabricLoader.getInstance().isModLoaded("jeed");

	public static boolean canClickEffect(MobEffectInstance effectInstance) {
		if (jeed) {
			return JeedCompat.canClickEffect(effectInstance);
		}
		return false;
	}

	public static void clickEffect(MobEffectInstance effectInstance, double mouseX, double mouseY, int button) {
		if (jeed) {
			JeedCompat.clickEffect(effectInstance, mouseX, mouseY, button);
		}
	}
}
