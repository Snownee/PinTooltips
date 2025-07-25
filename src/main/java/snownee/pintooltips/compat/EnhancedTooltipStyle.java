package snownee.pintooltips.compat;

import org.jetbrains.annotations.Nullable;

import dev.ultimatchamp.enhancedtooltips.kaleido.render.tooltip.impl.TooltipItemStackCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.DefaultTooltipStyle;
import snownee.pintooltips.PinnedTooltip;
import snownee.pintooltips.TooltipStyle;
import snownee.pintooltips.duck.PTGuiGraphics;

public class EnhancedTooltipStyle implements TooltipStyle {
	@Override
	public void updateSize(PinnedTooltip tooltip, int screenWidth, int screenHeight, Font font) {
		DefaultTooltipStyle.INSTANCE.updateSize(tooltip, screenWidth, screenHeight, font);
	}

	@Override
	public @Nullable Style getStyleAt(PinnedTooltip tooltip, double mouseX, double mouseY, Font font) {
		return null;
	}

//	@Override
//	public boolean isHovering(PinnedTooltip tooltip, double mouseX, double mouseY) {
//		return TooltipStyle.super.isHovering(tooltip, mouseX, mouseY);
//	}

	@Override
	public void preRender(PinnedTooltip tooltip, Screen screen, Font font, GuiGraphics context, int mouseX, int mouseY) {
		TooltipItemStackCache.saveItemStack(PTGuiGraphics.of(context).pin_tooltips$getRenderingItemStack());
	}

	@Override
	public void postRender(PinnedTooltip tooltip, Screen screen, Font font, GuiGraphics context, int mouseX, int mouseY) {
		TooltipItemStackCache.saveItemStack(ItemStack.EMPTY);
	}
}
