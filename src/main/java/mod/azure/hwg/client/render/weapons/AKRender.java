package mod.azure.hwg.client.render.weapons;

import mod.azure.hwg.client.models.weapons.AKModel;
import mod.azure.hwg.item.weapons.AssasultItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AKRender extends GeoItemRenderer<AssasultItem> {
	public AKRender() {
		super(new AKModel());
	}
}