package snownee.pintooltips;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.ModIdentification;
import snownee.pintooltips.mods.JeedCompat;
import snownee.pintooltips.mods.MEICompat;

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
			return MEICompat.canClickEnchantment(enchantment.value());
		}
		return false;
	}

	public static void clickEnchantment(Enchantment enchantment, int level, int button) {
		if (moreEnchantmentInfo) {
			MEICompat.clickEnchantment(enchantment, button);
		}
	}

	public static Component appendModName(Component desc, MobEffectInstance effectInstance) {
		if (!PinTooltipsConfig.jadeModMobEffectModName || !shouldAppendModName()) {
			return desc;
		}
		var key = effectInstance.getEffect().getKey();
		if (key == null) {
			return desc;
		}
		return appendModName(desc, ModIdentification.getModName(key.location()));
	}

	public static Component appendModName(Component desc, Holder<Enchantment> enchantment) {
		if (!PinTooltipsConfig.jadeModEnchantmentModName || !shouldAppendModName()) {
			return desc;
		}
		var key = enchantment.getKey();
		if (key == null) {
			return desc;
		}
		return appendModName(desc, ModIdentification.getModName(key.location()));
	}

	private static Component appendModName(Component desc, String modName) {
		return desc.copy().append("\n").append(Component.literal(modName)
				.withStyle(IWailaConfig.get().getFormatting().getItemModNameStyle()));
	}

	public static boolean shouldAppendModName() {
		return jade && IWailaConfig.get().getGeneral().showItemModNameTooltip();
	}
}
