package mod.azure.hwg.client;

import mod.azure.hwg.client.render.BulletsRender;
import mod.azure.hwg.util.ProjectilesEntityRegister;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public class RenderRegistry {

	public static void init() {
		EntityRendererRegistry.INSTANCE.register(ProjectilesEntityRegister.BULLETS, (dispatcher, context) -> {
			return new BulletsRender(dispatcher);
		});
	}

}
