package snownee.pintooltips.mixin.interact;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.pintooltips.PinTooltipsHooks;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@WrapMethod(method = "getTooltipLines")
	private List<Component> pintooltips$handleGrabbing(
			final @Nullable Player player,
			final TooltipFlag isAdvanced,
			final Operation<List<Component>> original) {
		PinTooltipsHooks.markGrabbing();
		var result = original.call(player, isAdvanced);
		PinTooltipsHooks.unmarkGrabbing();
		return result;
	}
}
