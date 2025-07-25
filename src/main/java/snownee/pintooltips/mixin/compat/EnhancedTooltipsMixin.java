/*
package snownee.pintooltips.mixin.compat;

import java.util.List;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;

import dev.ultimatchamp.enhancedtooltips.EnhancedTooltipsDrawer;
import dev.ultimatchamp.enhancedtooltips.kaleido.render.tooltip.impl.TooltipItemStackCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.compat.EnhancedTooltipStyle;
import snownee.pintooltips.duck.PTGuiGraphics;

@Mixin(EnhancedTooltipsDrawer.class)
public class EnhancedTooltipsMixin {
	@Inject(method = "drawTooltip", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void pin_tooltips$onRender(
			GuiGraphics graphics,
			Font font,
			List<ClientTooltipComponent> components,
			int x,
			int y,
			ClientTooltipPositioner positioner,
			CallbackInfo ci,
			@Local Vector2ic position) {
		if (PTGuiGraphics.of(graphics).pin_tooltips$getRenderingPinned()) {
			return;
		}
		ItemStack itemStack = TooltipItemStackCache.getItemStack();
		if (itemStack == null) {
			itemStack = PTGuiGraphics.of(graphics).pin_tooltips$getRenderingItemStack();
		}
		components = Lists.newArrayList(components);
		components.add(new ClientTextTooltip(Component.literal("kaleido_tooltip_mark").getVisualOrderText()));
		PinTooltips.onRenderTooltip(font, components, position, itemStack, new EnhancedTooltipStyle());
	}
}
*/
