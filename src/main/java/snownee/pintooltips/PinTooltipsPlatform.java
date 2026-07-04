package snownee.pintooltips;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public final class PinTooltipsPlatform {
	private PinTooltipsPlatform() {
	}

	public static boolean isDevelopmentEnvironment() {
		return !FMLLoader.getCurrent().isProduction();
	}

	public static boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}
}
