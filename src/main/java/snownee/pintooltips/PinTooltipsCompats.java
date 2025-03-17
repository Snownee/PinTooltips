package snownee.pintooltips;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.ModIdentification;
import snownee.pintooltips.compat.JeedCompat;
import snownee.pintooltips.compat.MEICompat;

public class PinTooltipsCompats {
	public static boolean jeed = FabricLoader.getInstance().isModLoaded("jeed");
	public static boolean jade = FabricLoader.getInstance().isModLoaded("jade");
	public static boolean moreEnchantmentInfo = FabricLoader.getInstance().isModLoaded("more_enchantment_info");

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

	public static boolean canClickEnchantment(Holder<Enchantment> enchantment) {
		if (moreEnchantmentInfo) {
			return MEICompat.canClickEnchantment(enchantment);
		}
		return false;
	}

	public static void clickEnchantment(Enchantment enchantment, int level, int button) {
		if (moreEnchantmentInfo) {
			MEICompat.clickEnchantment(enchantment, button);
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

	public static Component appendModName(Component desc, Holder<Enchantment> enchantment) {
		if (!PinTooltipsConfig.get().jadeModEnchantmentModName() || !shouldAppendModName()) {
			return desc;
		}
		ResourceLocation key = enchantment.unwrapKey().orElseThrow().location();
		return appendModName(desc, ModIdentification.getModName(key));
	}

	private static Component appendModName(Component desc, String modName) {
		return desc.copy().append("\n").append(IWailaConfig.get().getFormatting().getModName().formatted(modName));
	}

	public static boolean shouldAppendModName() {
		return jade && IWailaConfig.get().getGeneral().showItemModNameTooltip();
	}
}
