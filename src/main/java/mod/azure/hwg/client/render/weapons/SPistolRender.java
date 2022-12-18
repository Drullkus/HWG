package mod.azure.hwg.client.render.weapons;

import mod.azure.hwg.client.models.weapons.SPistolModel;
import mod.azure.hwg.item.weapons.SPistolItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SPistolRender extends GeoItemRenderer<SPistolItem> {
	public SPistolRender() {
		super(new SPistolModel());
	}
}