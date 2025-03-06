package snownee.pintooltips;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(PinTooltips.ID)
public class PinTooltipsForge {
	public PinTooltipsForge() {
		if (FMLEnvironment.dist.isClient()) {
			PinTooltipsForgeClient.init();
		}
	}
}
