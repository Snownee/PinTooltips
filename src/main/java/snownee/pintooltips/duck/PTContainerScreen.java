package snownee.pintooltips.duck;

import net.minecraft.world.inventory.Slot;

public interface PTContainerScreen {
	void pin_tooltips$setDummyHoveredSlot(Slot slot);
	void pin_tooltips$dropDummyHoveredSlot();
}
