package snownee.pintooltips;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig(type = KiwiConfig.ConfigType.CLIENT)
public class PinTooltipsConfig {
	public static boolean hideMissingDescriptions = true;
	public static boolean jadeModEnchantmentModName = true;
	public static boolean jadeModMobEffectModName = true;
	public static int hoveringAutoPinDelay = 1500;
	@ConfigUI.Typed(String.class)
	public static List<String> screenBlacklist = defaultBlacklist();
	@KiwiModule.Skip
	public static Set<String> screenBlacklistSet = Set.copyOf(defaultBlacklist());

	@KiwiConfig.Listen("screenBlacklist")
	public static void blocklistChanged(String path) {
		screenBlacklistSet = Set.copyOf(screenBlacklist);
	}

	private static List<String> defaultBlacklist() {
		return List.of(
				PauseScreen.class.getName(),
				ChatScreen.class.getName(),
				GenericMessageScreen.class.getName(),
				ReceivingLevelScreen.class.getName(),
				ProgressScreen.class.getName()
		);
	}
}
