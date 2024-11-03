package snownee.pintooltips.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.PotionUtils;
import snownee.pintooltips.PinTooltips;
import snownee.pintooltips.util.DefaultDescriptions;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {
	@Inject(
			method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V",
			at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	private static void addPotionTooltip(
			List<MobEffectInstance> effects,
			List<Component> tooltips,
			float durationFactor,
			CallbackInfo ci,
			List list,
			Iterator var4,
			MobEffectInstance mobEffectInstance,
			MutableComponent component,
			MobEffect mobEffect,
			Map map,
			Iterator var9,
			Map.Entry entry,
			AttributeModifier attributeModifier,
			AttributeModifier attributeModifier2) {
		if (PinTooltips.isHoldingKey()) {
			Component desc = DefaultDescriptions.forStatusEffectFormatted(mobEffect);
			if (desc == null) {
				return;
			}
			component.withStyle($ -> $.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
		}
	}
}
