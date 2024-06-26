package com.nukateam.ntgl.client.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.nukateam.ntgl.client.animators.GunItemAnimator;
import com.nukateam.ntgl.client.model.GeoGunModel;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.render.layers.LocalPlayerSkinLayer;
import com.nukateam.ntgl.Ntgl;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.*;

public class GunRendererDynamic extends GeoDynamicItemRenderer<GunItemAnimator> {
    public static final int PACKED_OVERLAY = 15728880;
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    private TransformType transformType;
    private MultiBufferSource bufferSource;
    private ItemStack renderStack;
    private boolean renderHands = false;
    protected LivingEntity buffEntity = null;

    public GunRendererDynamic() {
        super(new GeoGunModel<>(), GunItemAnimator::new);
        addRenderLayer(new GlowingLayer<>(this));
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
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.renderStack = stack;
        this.renderHands = transformType == FIRST_PERSON_RIGHT_HAND || transformType == FIRST_PERSON_LEFT_HAND;

        if(buffEntity != null){
            entity = buffEntity;
            buffEntity = null;
        }

        if(entity instanceof Raider){
            Ntgl.LOGGER.debug("");
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
        var client = Minecraft.getInstance();
        var renderArms = false;

        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms = true;
            }
        }

        if (renderArms && this.transformType == TransformType.FIRST_PERSON_RIGHT_HAND || this.transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
            var playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
            var playerEntityModel = playerEntityRenderer.getModel();
            poseStack.pushPose();
            {
                RenderUtils.prepMatrixForBone(poseStack, bone);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
//                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
                poseStack.translate(0.02, -0.44, -0.15);
//                poseStack.translate(X, Y, Z);

                assert (client.player != null);

                var playerSkin = ((LocalPlayer) ClientUtils.getClientPlayer()).getSkinTextureLocation();
                var arm = this.bufferSource.getBuffer(RenderType.entitySolid(playerSkin));
                var sleeve = this.bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));

                if (bone.getName().equals(LEFT_ARM)) {
                    playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.leftArm.setRotation(0, 0, 0);
                    playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                    playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.leftSleeve.setRotation(0, 0, 0);
                    playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
                } else if (bone.getName().equals(RIGHT_ARM)) {
                    playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.rightArm.setRotation(0, 0, 0);
                    playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

                    playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                    playerEntityModel.rightSleeve.setRotation(0, 0, 0);
                    playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
                }
            }
            poseStack.popPose();
        }
        // This super call is needed with the custom getBuffer call for the weapon model to get it's texture back and not use the players skin
        super.renderRecursively(poseStack, animatable, bone, renderType,
                bufferSource, this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, red, green, blue, alpha);
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
