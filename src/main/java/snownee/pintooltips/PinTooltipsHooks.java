package snownee.pintooltips;

import net.minecraft.world.item.ItemStack;

public class PinTooltipsHooks {
	public static ItemStack renderingItemStack = ItemStack.EMPTY;

	private static final ThreadLocal<Boolean> isGrabbing = new ThreadLocal<>();

	public static void markGrabbing() {
		if (PinTooltips.isGrabbing()) {
			isGrabbing.set(true);
		}
	}

	public static void unmarkGrabbing() {
		isGrabbing.set(false);
	}

	public static boolean isGrabbing() {
		return isGrabbing.get();
	}
}
