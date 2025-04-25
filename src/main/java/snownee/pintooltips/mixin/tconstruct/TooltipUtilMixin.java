package snownee.pintooltips.mixin.tconstruct;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import snownee.pintooltips.PinTooltipsHooks;

@Mixin(value = TooltipUtil.class, remap = false)
public class TooltipUtilMixin {
	@WrapOperation(
			method = "addModifierNames",
			at = @At(
					value = "INVOKE",
					target = "Lslimeknights/tconstruct/library/modifiers/Modifier;getDisplayName(Lslimeknights/tconstruct/library/tools/nbt/IToolStackView;Lslimeknights/tconstruct/library/modifiers/ModifierEntry;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/network/chat/Component;"))
	private static Component pin_tooltips$addModifierNames(
			Modifier instance,
			IToolStackView tool,
			ModifierEntry entry,
			RegistryAccess access,
			Operation<Component> original) {
		Component component = original.call(instance, tool, entry, access);
		if (PinTooltipsHooks.isGrabbing()) {
			List<Component> list = instance.getDescriptionList(tool, entry);
			MutableComponent first = Component.empty().append(list.get(0));
			MutableComponent desc = list.stream()
					.skip(1)
					.map(Component::copy)
					.reduce(first, (a, b) -> a.append("\n").append(b));
			HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc);
			component = component.copy().withStyle($ -> $.withUnderlined(true).withHoverEvent(hoverEvent));
		}
		return component;
	}

}
