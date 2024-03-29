package com.nukateam.gunscore.client.render.layers;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.gunscore.client.data.util.TextureUtils.getTextureSize;
import static mod.azure.azurelib.cache.texture.GeoAbstractTexture.appendToPath;

public class GlowingLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private Pair<Integer, Integer> size;

    public GlowingLayer(GeoRenderer<T> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if(size == null) this.size = getTextureSize(getRenderer().getTextureLocation(animatable));
//        poseStack.translate(-0.5, -0.5, -0.5);

        var model = getRenderer().getGeoModel();
        var texture = appendToPath(model.getTextureResource(animatable), "_glowmask");
        renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);

//        if(model instanceof IGlowingModel glowingModel) {
//            var TESLA_TEXTURE = glowingModel.getGlowingTextureResource(animatable);
//            if (TESLA_TEXTURE != null)
//                renderLayer(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, TESLA_TEXTURE);
//        }
    }

    protected void renderLayer(PoseStack poseStack, T entity, BakedGeoModel bakedModel, MultiBufferSource bufferSource,
                               float partialTick, int packedLight, ResourceLocation texture) {
        int overlay = OverlayTexture.NO_OVERLAY;
        RenderType renderTypeNew = RenderType.eyes(texture);
        poseStack.pushPose();
        poseStack.scale(1.0F, 1.0F, 1.0F);
        poseStack.translate(0.0, 0.0, 0.0);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, entity, renderTypeNew, bufferSource.getBuffer(renderTypeNew), partialTick, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}