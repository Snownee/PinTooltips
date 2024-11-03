package snownee.pintooltips.mixin.interact;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsCompats;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow
	@Nullable
	protected Minecraft minecraft;

	@Inject(
			method = "handleComponentClicked",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasShiftDown()Z"),
			cancellable = true)
	private void handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
		ClickEvent clickEvent = Objects.requireNonNull(style.getClickEvent());
		if (clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
			return;
		}
		String value = clickEvent.getValue();
		if (!value.startsWith("@pin_tooltips ")) {
			return;
		}
		value = value.substring(14);
		if (value.startsWith("click_effect ")) {
			value = value.substring(13);
			try {
				MobEffectInstance effectInstance = MobEffectInstance.load(TagParser.parseTag(value));
				if (effectInstance == null) {
					return;
				}
				Objects.requireNonNull(minecraft);
				Window window = minecraft.getWindow();
				double mouseX = minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
				double mouseY = minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
				PinTooltipsCompats.clickEffect(effectInstance, mouseX, mouseY, InputConstants.MOUSE_BUTTON_LEFT);
			} catch (Exception e) {
				PinTooltips.LOGGER.error("Failed to parse action", e);
			}
		}
		cir.setReturnValue(true);
	}
}
