package snownee.pintooltips.mixin.interact;

import java.util.Objects;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsCompats;
import snownee.pintooltips.PinTooltipsHooks;
import snownee.pintooltips.PinnedTooltipsService;
import snownee.pintooltips.util.ComponentDecorator;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(
			method = "defaultHandleGameClickEvent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"),
			cancellable = true)
	private static void pin_tooltips$defaultHandleGameClickEvent(
			ClickEvent event,
			Minecraft minecraft,
			Screen activeScreen,
			CallbackInfo ci,
			@Local(name = "custom") ClickEvent.Custom custom) {
		Optional<Tag> tag = custom.payload();
		if (tag.isEmpty()) {
			return;
		}
		Identifier id = custom.id();
		if (!id.getNamespace().equals(PinTooltips.ID)) {
			return;
		}
		try {
			if (ComponentDecorator.SHOW_EFFECT.equals(id)) {
				MobEffectInstance effectInstance = MobEffectInstance.CODEC.parse(NbtOps.INSTANCE, tag.get()).result().orElse(null);
				if (effectInstance == null) {
					return;
				}
				Window window = minecraft.getWindow();
				double mouseX = minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
				double mouseY = minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
				PinTooltipsCompats.clickEffect(effectInstance, mouseX, mouseY, InputConstants.MOUSE_BUTTON_LEFT);
			} else if (ComponentDecorator.SHOW_ENCHANTMENT.equals(id)) {
				CompoundTag compoundTag = (CompoundTag) tag.get();
				Enchantment enchantment = Objects.requireNonNull(Minecraft.getInstance().level)
						.registryAccess()
						.lookupOrThrow(Registries.ENCHANTMENT)
						.getValue(Identifier.parse(compoundTag.getString("enchantment").orElseThrow()));
				if (enchantment == null) {
					return;
				}
				int level = compoundTag.getInt("level").orElseThrow();
				PinTooltipsCompats.clickEnchantment(enchantment, level, InputConstants.MOUSE_BUTTON_LEFT);
			}
		} catch (Throwable e) {
			PinTooltips.LOGGER.error("Failed to parse component action", e);
		}
		ci.cancel();
	}

	@WrapMethod(method = "renderWithTooltipAndSubtitles")
	private void pin_tooltips$renderWithTooltip(GuiGraphics graphics, int mouseX, int mouseY, float a, Operation<Void> original) {
		boolean grabbing = PinTooltipsHooks.markGrabbing();
		if (PinnedTooltipsService.INSTANCE.hovered != null) {
			mouseX = Integer.MAX_VALUE;
			mouseY = Integer.MAX_VALUE;
		}
		original.call(graphics, mouseX, mouseY, a);
		PinTooltipsHooks.unmarkGrabbing(grabbing);
	}
}
