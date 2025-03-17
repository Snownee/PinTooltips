package snownee.pintooltips;

public class PinTooltipsHooks {
	private static final ThreadLocal<Integer> isGrabbing = ThreadLocal.withInitial(() -> 0);

	public static boolean markGrabbing() {
		if (PinTooltips.isGrabbing()) {
			isGrabbing.set(isGrabbing.get() + 1);
			return true;
		}
		return false;
	}

	public static void unmarkGrabbing(boolean grabbing) {
		if (grabbing) {
			int i = isGrabbing.get();
			if (i > 0) {
				isGrabbing.set(i - 1);
			}
		}
	}

	public static boolean isGrabbing() {
		return isGrabbing.get() > 0;
	}
}
