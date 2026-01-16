package snownee.pintooltips.util;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Suppliers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DummyHoveredSlot extends Slot {
	private static final Supplier<Container> CONTAINER = Suppliers.memoize(() -> new Container() {
		@Override
		public int getContainerSize() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public @NotNull ItemStack getItem(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public @NotNull ItemStack removeItem(int slot, int amount) {
			return ItemStack.EMPTY;
		}

		@Override
		public @NotNull ItemStack removeItemNoUpdate(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setItem(int slot, ItemStack stack) {}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(Player player) {
			return false;
		}

		@Override
		public void clearContent() {}
	});
	private final ItemStack itemStack;

	public DummyHoveredSlot(ItemStack itemStack) {
		super(CONTAINER.get(), 99999, 0, 0);
		this.itemStack = itemStack;
	}

	@Override
	public @NotNull ItemStack getItem() {
		return itemStack;
	}

	@Override
	public boolean allowModification(Player player) {
		return false;
	}

	@Override
	public boolean isHighlightable() {
		return false;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}
}
