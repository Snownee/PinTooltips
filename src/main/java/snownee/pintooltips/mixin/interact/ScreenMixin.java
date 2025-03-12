package snownee.pintooltips.mixin.interact;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsCompats;
import snownee.pintooltips.PinTooltipsHooks;

@Mixin(Screen.class)
public class ScreenMixin {
	@Shadow
	@Nullable
	protected Minecraft minecraft;

	@Inject(
			method = "handleComponentClicked",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasShiftDown()Z"),
			cancellable = true)
	private void pin_tooltips$handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
		ClickEvent clickEvent = style.getClickEvent();
		if (clickEvent == null || minecraft == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
			return;
		}
		String value = clickEvent.getValue();
		if (!value.startsWith("@pin_tooltips ")) {
			return;
		}
		try {
			value = value.substring(14);
			if (value.startsWith("click_effect ")) {
				value = value.substring(13);
				MobEffectInstance effectInstance = MobEffectInstance.load(TagParser.parseTag(value));
				if (effectInstance == null) {
					return;
				}
				Window window = minecraft.getWindow();
				double mouseX = minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
				double mouseY =
						minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
				PinTooltipsCompats.clickEffect(effectInstance, mouseX, mouseY, InputConstants.MOUSE_BUTTON_LEFT);
			} else if (value.startsWith("click_enchantment ")) {
				String[] parts = StringUtils.split(value.substring(18), " ");
				Optional<Enchantment> enchantment = Objects.requireNonNull(Minecraft.getInstance().level)
						.registryAccess()
						.registry(Registries.ENCHANTMENT)
						.flatMap(it -> it.getOptional(ResourceLocation.parse(parts[0])));
				if (enchantment.isEmpty()) {
					return;
				}
				int level = Integer.parseInt(parts[1]);
				PinTooltipsCompats.clickEnchantment(enchantment.get(), level, InputConstants.MOUSE_BUTTON_LEFT);

			}
		} catch (Throwable e) {
			PinTooltips.LOGGER.error("Failed to parse component action", e);
		}
		cir.setReturnValue(true);
	}

	@WrapMethod(method = "renderWithTooltip")
	private void pin_tooltips$renderWithTooltip(
			GuiGraphics guiGraphics,
			int mouseX,
			int mouseY,
			float partialTick,
			Operation<Void> original) {
		boolean grabbing = PinTooltipsHooks.markGrabbing();
		original.call(guiGraphics, mouseX, mouseY, partialTick);
		PinTooltipsHooks.unmarkGrabbing(grabbing);
	}
}
