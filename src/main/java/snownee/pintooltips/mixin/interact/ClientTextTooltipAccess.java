package snownee.pintooltips.mixin.interact;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;

@Mixin(ClientTextTooltip.class)
public interface ClientTextTooltipAccess {
	@Accessor
	FormattedCharSequence getText();
}
