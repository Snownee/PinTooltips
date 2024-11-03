package snownee.pintooltips;

public class PinTooltipsHooks {
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
