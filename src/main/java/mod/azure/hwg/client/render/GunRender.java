package mod.azure.hwg.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.common.api.client.renderer.GeoItemRenderer;
import mod.azure.azurelib.common.api.client.renderer.layer.AutoGlowingGeoLayer;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.hwg.HWGMod;
import mod.azure.hwg.client.models.GunModel;
import mod.azure.hwg.item.enums.GunTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

public class GunRender<T extends Item & GeoItem> extends GeoItemRenderer<T> {
    private final GunTypeEnum gunTypeEnum;

    public GunRender(String id, GunTypeEnum gunTypeEnum) {
        super(new GunModel<>(HWGMod.modResource(id + "/" + id), gunTypeEnum));
        this.gunTypeEnum = gunTypeEnum;
        if (gunTypeEnum == GunTypeEnum.BRIMSTONE || gunTypeEnum == GunTypeEnum.BALROG)
			this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (gunTypeEnum == GunTypeEnum.ROCKETLAUNCHER)
            model.getBone("rocket").get().setHidden(this.currentItemStack.getDamageValue() == (this.currentItemStack.getMaxDamage() - 1));
    }

    @Override
	protected void renderInGui(ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (bufferSource instanceof MultiBufferSource.BufferSource defaultBufferSource) {
            RenderType renderType = this.getRenderType(this.animatable, this.getTextureLocation(this.animatable), defaultBufferSource, Minecraft.getInstance().getFrameTime());
            VertexConsumer buffer = ItemRenderer.getFoilBufferDirect(bufferSource, renderType, true, this.currentItemStack != null && this.currentItemStack.hasFoil());

            poseStack.pushPose();
            //Lighting.setupFor3DItems(); //Lighting.setupForFlatItems();
            this.defaultRender(poseStack, this.animatable, defaultBufferSource, renderType, buffer, 0, Minecraft.getInstance().getFrameTime(), packedLight);
            //defaultBufferSource.endBatch();
            //RenderSystem.enableDepthTest();
            //Lighting.setupFor3DItems();
            poseStack.popPose();
        }
    }
}