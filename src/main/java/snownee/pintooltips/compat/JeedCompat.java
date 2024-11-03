package snownee.pintooltips.compat;

import net.mehvahdjukaar.jeed.Jeed;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class JeedCompat {
	private static final TagKey<MobEffect> HIDDEN = TagKey.create(Registries.MOB_EFFECT, new ResourceLocation("jeed:hidden"));

	public static boolean canClickEffect(MobEffectInstance effectInstance) {
		return Jeed.PLUGIN != null && !Jeed.isTagged(effectInstance.getEffect(), BuiltInRegistries.MOB_EFFECT, HIDDEN);
	}

	public static void clickEffect(MobEffectInstance effectInstance, double mouseX, double mouseY, int button) {
		if (Jeed.PLUGIN != null) {
			Jeed.PLUGIN.onClickedEffect(effectInstance, mouseX, mouseY, button);
		}
	}
}
