package snownee.pintooltips.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public interface ForgeGuiGraphicsAccess {
	@Accessor(remap = false)
	void setTooltipStack(ItemStack itemStack);
}
