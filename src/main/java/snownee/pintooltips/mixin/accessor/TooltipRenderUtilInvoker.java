package snownee.pintooltips.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;

import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TooltipRenderUtil.class)
public interface TooltipRenderUtilInvoker {
	@Invoker("getFrameSprite")
	static Identifier callGetFrameSprite(Identifier name) {
		throw new AssertionError();
	}
}
