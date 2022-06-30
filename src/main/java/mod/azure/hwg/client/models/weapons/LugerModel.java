package mod.azure.hwg.client.models.weapons;

import mod.azure.hwg.HWGMod;
import mod.azure.hwg.item.weapons.LugerItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LugerModel extends AnimatedGeoModel<LugerItem> {
	@Override
	public Identifier getModelResource(LugerItem object) {
		return new Identifier(HWGMod.MODID, "geo/luger.geo.json");
	}

	@Override
	public Identifier getTextureResource(LugerItem object) {
		return new Identifier(HWGMod.MODID, "textures/items/luger.png");
	}

	@Override
	public Identifier getAnimationResource(LugerItem animatable) {
		return new Identifier(HWGMod.MODID, "animations/pistol.animation.json");
	}
}
