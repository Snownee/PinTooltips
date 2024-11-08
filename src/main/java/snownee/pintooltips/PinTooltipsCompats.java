package snownee.pintooltips;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.ModIdentification;
import snownee.pintooltips.compat.JeedCompat;

public class PinTooltipsCompats {
	public static boolean jeed = FabricLoader.getInstance().isModLoaded("jeed");
	public static boolean jade = FabricLoader.getInstance().isModLoaded("jade");

	public static boolean canClickEffect(MobEffectInstance effectInstance) {
		if (jeed) {
			return JeedCompat.canClickEffect(effectInstance);
		}
		return false;
	}

	public static void clickEffect(MobEffectInstance effectInstance, double mouseX, double mouseY, int button) {
		if (jeed) {
			JeedCompat.clickEffect(effectInstance, mouseX, mouseY, button);
		}
	}

	public static Component appendModName(Component desc, MobEffectInstance effectInstance) {
		if (!PinTooltipsConfig.get().jadeModEnchantmentModName() || !shouldAppendModName()) {
			return desc;
		}
		ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect());
		if (key == null) {
			return desc;
		}
		return appendModName(desc, ModIdentification.getModName(key));
	}

	public static Component appendModName(Component desc, Enchantment enchantment) {
		if (!PinTooltipsConfig.get().jadeModEnchantmentModName() || !shouldAppendModName()) {
			return desc;
		}
		ResourceLocation key = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
		if (key == null) {
			return desc;
		}
		return appendModName(desc, ModIdentification.getModName(key));
	}

	private static Component appendModName(Component desc, String modName) {
		return desc.copy().append("\n").append(IWailaConfig.get().getFormatting().getModName().formatted(modName));
	}

	public static boolean shouldAppendModName() {
		return jade && IWailaConfig.get().getGeneral().showItemModNameTooltip();
	}
}
