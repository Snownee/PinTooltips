package snownee.pintooltips.mods;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;

public class MEICompat {
	private static Object INSTANCE;
	private static Method METHOD;

	private static Object getInstance() {
		if (INSTANCE == null) {
			try {
				Class<?> clazz = Class.forName("settingdust.more_enchantment_info.MoreEnchantmentInfoClient");
				INSTANCE = clazz.getDeclaredField("INSTANCE").get(null);
				METHOD = clazz.getDeclaredMethod("viewEnchantment", Enchantment.class);
			} catch (Throwable e) {
				PinTooltips.LOGGER.error("Failed to get instance of MoreEnchantmentInfoClient", e);
			}
		}
		return INSTANCE;
	}

	public static boolean canClickEnchantment(Enchantment enchantment) {
		return isAvailable();
	}

	public static void clickEnchantment(Enchantment enchantment, int button) {
		if (isAvailable()) {
			try {
				METHOD.invoke(getInstance(), enchantment);
			} catch (Throwable e) {
				PinTooltips.LOGGER.error("Failed to invoke viewEnchantment", e);
			}
		}
	}

	private static boolean isAvailable() {
		return getInstance() != null && Minecraft.getInstance().level != null;
	}
}
