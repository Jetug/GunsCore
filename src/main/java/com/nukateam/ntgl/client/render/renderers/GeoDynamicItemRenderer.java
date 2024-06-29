package com.nukateam.ntgl.client.render.renderers;

import com.nukateam.ntgl.client.animators.ItemAnimator;
import com.mojang.blaze3d.vertex.*;

import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.model.data.EntityModelData;
import mod.azure.azurelib.renderer.GeoObjectRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;


public class GeoDynamicItemRenderer<T extends ItemAnimator> extends GeoObjectRenderer<T> {
    private final Map<Pair<LivingEntity, ItemDisplayContext>, T> animatorsByTransform = new HashMap<>();
    private final BiFunction<ItemDisplayContext, GeoDynamicItemRenderer<T>, T> animatorFactory;
    protected ItemStack currentItemStack;
    protected ItemDisplayContext currentTransform;
    protected LivingEntity currentEntity;

    public GeoDynamicItemRenderer(GeoModel<T> model, BiFunction<ItemDisplayContext, GeoDynamicItemRenderer<T>, T> animatorFactory) {
        super(model);
        this.animatorFactory = animatorFactory;
    }

    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType,
                       PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType,
                       @Nullable VertexConsumer buffer,
                       int packedLight) {
        this.currentItemStack = stack;
        this.currentTransform = transformType;
        this.currentEntity = entity;
        super.render(poseStack, getRenderItem(entity, transformType), bufferSource, renderType, buffer, packedLight);
    }

    public T getRenderItem(LivingEntity entity, ItemDisplayContext transformType) {
        var key = Pair.of(entity, transformType);

        if (!animatorsByTransform.containsKey(key))
            animatorsByTransform.put(key, animatorFactory.apply(transformType, this));

        return animatorsByTransform.get(key);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        {
            boolean shouldSit = false;
            float lerpBodyRot = 0;
            float lerpHeadRot = 0;
            float netHeadYaw = lerpHeadRot - lerpBodyRot;
            float limbSwingAmount;
            limbSwingAmount = 0.0F;
            float limbSwing = 0.0F;

            if (!isReRender)
                setupRender(animatable, isReRender, partialTick, shouldSit, netHeadYaw, limbSwingAmount, limbSwing);

            poseStack.translate(0.0, 0.009999999776482582, 0.0);
            this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
        poseStack.popPose();
    }

    private void setupRender(T animatable, boolean isReRender, float partialTick, boolean shouldSit, float netHeadYaw, float limbSwingAmount, float limbSwing) {
        var headPitch = 0;
        var motionThreshold = 0;
        var velocity = Vec3.ZERO;//jetug
        var avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
        var animationState = new AnimationState(animatable, limbSwing, limbSwingAmount, partialTick,
                avgVelocity >= motionThreshold && limbSwingAmount != 0.0F);
        var instanceId = this.getInstanceId(animatable);

        animationState.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
        animationState.setData(DataTickets.ITEMSTACK, this.currentItemStack);
//            animatable.getAnimatableInstanceCache().getManagerForId(instanceId).setData(DataTickets.ITEM_RENDER_PERSPECTIVE, this.currentTransform);
        animationState.setData(DataTickets.TICK, animatable.getTick(animatable));
        animationState.setData(DataTickets.ENTITY, currentEntity);
        animationState.setData(DataTickets.ENTITY_MODEL_DATA, new EntityModelData(shouldSit, false, -netHeadYaw, -headPitch));
        var var31 = this.model;
        Objects.requireNonNull(animationState);
        var31.addAdditionalStateData(animatable, instanceId, animationState::setData);
        this.model.handleAnimations(animatable, instanceId, animationState);
    }
}
