package mod.azure.hwg.client.render.weapons;

import mod.azure.hwg.client.models.weapons.PistolModel;
import mod.azure.hwg.item.weapons.PistolItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PistolRender extends GeoItemRenderer<PistolItem> {
	public PistolRender() {
		super(new PistolModel());
	}
}