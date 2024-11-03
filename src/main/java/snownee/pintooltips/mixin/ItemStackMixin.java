package snownee.pintooltips.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.pintooltips.PinTooltips;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void getTooltipLinesPre(Player player, TooltipFlag isAdvanced, CallbackInfoReturnable<List<Component>> cir) {
		PinTooltips.getTooltipLinesPre();
	}

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void getTooltipLinesPost(Player player, TooltipFlag isAdvanced, CallbackInfoReturnable<List<Component>> cir) {
		PinTooltips.getTooltipLinesPost();
	}
}
