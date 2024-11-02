package snownee.pintooltips.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccess {
    @Invoker
    void callRenderTooltipInternal(
        Font font,
        List<ClientTooltipComponent> components,
        int mouseX,
        int mouseY,
        ClientTooltipPositioner tooltipPositioner
    );
}
