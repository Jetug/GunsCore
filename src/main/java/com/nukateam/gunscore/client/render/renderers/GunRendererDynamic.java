package com.nukateam.gunscore.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.gunscore.client.animators.GunItemAnimator;
import com.nukateam.gunscore.client.model.GeoGunModel;
import com.nukateam.gunscore.client.render.layers.GlowingLayer;
import com.nukateam.gunscore.client.render.layers.LocalPlayerSkinLayer;
import com.nukateam.gunscore.GunMod;
import mod.azure.azurelib.cache.object.GeoBone;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.nukateam.gunscore.client.data.handler.GunRenderingHandler.getAttachmentNames;
import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.*;

public class GunRendererDynamic extends GeoDynamicItemRenderer<GunItemAnimator> {
    public static final int PACKED_OVERLAY = 15728880;
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";

    private ItemStack renderStack;
    private boolean renderHands = false;
    protected LivingEntity buffEntity = null;

    public GunRendererDynamic() {
        super(new GeoGunModel<>(), GunItemAnimator::new);
        addRenderLayer(new LocalPlayerSkinLayer<>(this));
        addRenderLayer(new GlowingLayer<>(this));
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public ItemStack getRenderStack() {
        return renderStack;
    }

    public LivingEntity getRenderEntity() {
        return currentEntity;
    }

    public void setEntity(LivingEntity entity) {
        this.buffEntity = entity;
    }

//    @Override
//    public float getMotionAnimThreshold(GunItemAnimator animatable) {
//        return 0.005f;
//    }

    @Override
    public void render(LivingEntity entity, ItemStack stack, TransformType transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {

        this.renderStack = stack;
        this.renderHands = transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND;

        if(buffEntity != null){
            entity = buffEntity;
            buffEntity = null;
        }

        if(entity instanceof Raider){
            GunMod.LOGGER.debug("");
        }

//        poseStack.pushPose();
        switch (transformType) {
//            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
//                if (entity instanceof PowerArmorFrame)
//                    poseStack.translate(0, 0.07, -0.25);
//            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
//                poseStack.translate(0, -1.3, -1.55);
            }
            case GUI -> {
//                poseStack.translate(0.2, -0.55, -0.5);
//                poseStack.translate(0.2, 0.1, -0.5);
            }
            case GROUND -> {
//                poseStack.translate(-0.5, -0.5, -1);
            }
        }

        renderAttachments(stack, getRenderItem(entity, transformType));

        super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
//        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, GunItemAnimator animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                  int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if(bone.getName().equals(RIGHT_ARM) || bone.getName().equals(LEFT_ARM)){
            bone.setHidden(!renderHands);
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    protected void renderAttachments(ItemStack stack, GunItemAnimator item) {
//        var config = item.getConfig();
//
//        if (config != null) {
//            var allMods = config.mods;
////            var visibleMods = StackUtils.getAttachments(stack);
//            var names = getAttachmentNames(stack);
//
//            for (var name : allMods)
//                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(true));
////            for (var name : visibleMods)
////                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(false));
//            for (var name : names)
//                getGeoModel().getBone(name).ifPresent((bone) -> bone.setHidden(false));
//        }
    }
}
