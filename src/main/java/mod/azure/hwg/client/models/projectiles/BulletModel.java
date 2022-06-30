package mod.azure.hwg.client.models.projectiles;

import mod.azure.hwg.HWGMod;
import mod.azure.hwg.entity.projectiles.BulletEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BulletModel extends AnimatedGeoModel<BulletEntity> {
	@Override
	public Identifier getModelResource(BulletEntity object) {
		return new Identifier(HWGMod.MODID, "geo/bullet.geo.json");
	}

	@Override
	public Identifier getTextureResource(BulletEntity object) {
		return new Identifier(HWGMod.MODID, "textures/items/bullet.png");
	}

	@Override
	public Identifier getAnimationResource(BulletEntity animatable) {
		return new Identifier(HWGMod.MODID, "animations/bullet.animation.json");
	}
}
