package snownee.pintooltips.mods;

import net.mehvahdjukaar.jeed.api.JeedAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;

public class JeedCompat {
	public static boolean canClickEffect(MobEffectInstance effectInstance) {
		return isAvailable() && !JeedAPI.isEffectHidden(effectInstance.getEffect());
	}

	public static void clickEffect(MobEffectInstance effectInstance, double mouseX, double mouseY, int button) {
		if (isAvailable()) {
			JeedAPI.invokeEffectClicked(effectInstance, mouseX, mouseY, button);
		}
	}

	private static boolean isAvailable() {
		return Minecraft.getInstance().level != null;
	}
}
