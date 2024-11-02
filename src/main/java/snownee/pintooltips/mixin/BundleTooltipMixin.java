package snownee.pintooltips.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.base.Objects;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinnableTooltipComponent;

/**
 * Not working since ItemStack has no hashCode
 */
@Mixin(BundleTooltip.class)
public class BundleTooltipMixin implements PinnableTooltipComponent {
	@Shadow
	@Final
	private NonNullList<ItemStack> items;
	@Shadow
	@Final
	private int weight;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof BundleTooltip that))
			return false;
		return weight == that.getWeight() && Objects.equal(items, that.getItems());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(items, weight);
	}
}
