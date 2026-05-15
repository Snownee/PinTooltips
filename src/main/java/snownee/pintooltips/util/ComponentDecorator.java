package snownee.pintooltips.util;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.PinTooltipsCompats;
import snownee.pintooltips.PinTooltipsConfig;

public class ComponentDecorator {
	public static final Identifier SHOW_EFFECT = PinTooltips.id("show_effect");
	public static final Identifier SHOW_ENCHANTMENT = PinTooltips.id("show_enchantment");
	public static final Identifier BUNDLE_ACTION = PinTooltips.id("bundle_action");

	public static void mobEffect(MutableComponent component, MobEffectInstance effectInstance) {
		Component desc = DefaultDescriptions.forStatusEffectFormatted(effectInstance.getEffect().value());
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, effectInstance);
		HoverEvent hoverEvent = new HoverEvent.ShowText(desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEffect(effectInstance)) {
			MobEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, effectInstance).ifSuccess(tag -> {
				component.withStyle($ -> $.withClickEvent(new ClickEvent.Custom(SHOW_EFFECT, Optional.of(tag))));
			});
		}
	}

	public static void enchantment(MutableComponent component, Holder<Enchantment> enchantment, int level) {
		var desc = DefaultDescriptions.forEnchantmentFormatted(enchantment);
		if (desc == null) {
			return;
		}
		desc = PinTooltipsCompats.appendModName(desc, enchantment);
		HoverEvent hoverEvent = new HoverEvent.ShowText(desc);
		component.withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		if (PinTooltipsCompats.canClickEnchantment(enchantment)) {
			CompoundTag tag = new CompoundTag();
			tag.putString("enchantment", enchantment.unwrapKey().orElseThrow().identifier().toString());
			tag.putInt("level", level);
			component.withStyle($ -> $.withClickEvent(new ClickEvent.Custom(SHOW_ENCHANTMENT, Optional.of(tag))));
		}
	}

	public static void handleClickEvent(Screen activeScreen, Identifier id, @Nullable Tag tag) {
		if (tag == null) {
			return;
		}
		try {
			if (SHOW_EFFECT.equals(id)) {
				MobEffectInstance effectInstance = MobEffectInstance.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
				if (effectInstance == null) {
					return;
				}
				Minecraft mc = Minecraft.getInstance();
				Window window = mc.getWindow();
				double mouseX = mc.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
				double mouseY = mc.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
				PinTooltipsCompats.clickEffect(effectInstance, mouseX, mouseY, InputConstants.MOUSE_BUTTON_LEFT);
			} else if (SHOW_ENCHANTMENT.equals(id)) {
				CompoundTag compoundTag = (CompoundTag) tag;
				Enchantment enchantment = Objects.requireNonNull(Minecraft.getInstance().level)
						.registryAccess()
						.lookupOrThrow(Registries.ENCHANTMENT)
						.getValue(Identifier.parse(compoundTag.getString("enchantment").orElseThrow()));
				if (enchantment == null) {
					return;
				}
				int level = compoundTag.getInt("level").orElseThrow();
				PinTooltipsCompats.clickEnchantment(enchantment, level, InputConstants.MOUSE_BUTTON_LEFT);
			} else if (PinTooltipsConfig.bundleInteraction && BUNDLE_ACTION.equals(id) &&
					activeScreen instanceof AbstractContainerScreen<?> screen) {
				int slotId = ((CompoundTag) tag).getInt("slot").orElseThrow();
				int index = ((CompoundTag) tag).getInt("index").orElseThrow();
				Slot slot = screen.getMenu().getSlot(slotId);
				ItemStack itemStack = slot.getItem();
				if (itemStack.has(DataComponents.BUNDLE_CONTENTS)) {
					ItemStack carried = screen.getMenu().getCarried();
					ClientPacketListener connection = Objects.requireNonNull(Minecraft.getInstance().getConnection());
					if (carried.isEmpty() && slotId != -1) {
						BundleItem.toggleSelectedItem(itemStack, -1);
						BundleItem.toggleSelectedItem(itemStack, index);
						connection.send(new ServerboundSelectBundleItemPacket(slotId, index));
						screen.slotClicked(slot, slotId, InputConstants.MOUSE_BUTTON_RIGHT, ContainerInput.PICKUP);
					} else if (!carried.isEmpty()) {
						screen.slotClicked(slot, slotId, InputConstants.MOUSE_BUTTON_LEFT, ContainerInput.PICKUP);
					}
				}
			}
		} catch (Throwable e) {
			PinTooltips.LOGGER.error("Failed to parse component action", e);
		}
	}
}
