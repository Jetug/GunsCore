package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.IHeldAnimation;
import com.nukateam.ntgl.client.render.pose.BazookaPose;
import com.nukateam.ntgl.client.render.pose.MiniGunPose;
import com.nukateam.ntgl.client.render.pose.OneHandedPose;
import com.nukateam.ntgl.client.render.pose.TwoHandedPose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class GripType {
    /**
     * A grip type designed for weapons that are held with only one hand, like a pistol
     */
    public static final GripType ONE_HANDED = new GripType(new ResourceLocation(Ntgl.MOD_ID, "one_handed"), new OneHandedPose());

    /**
     * A grip type designed for weapons that are held with two hands, like an assault rifle
     */
    public static final GripType TWO_HANDED = new GripType(new ResourceLocation(Ntgl.MOD_ID, "two_handed"), new TwoHandedPose());

    /**
     * A custom grip type designed for the mini gun simply due it's nature of being a completely
     * unique way to hold the weapon
     */
    public static final GripType MINI_GUN = new GripType(new ResourceLocation(Ntgl.MOD_ID, "mini_gun"), new MiniGunPose());

    /**
     * A custom grip type designed for the bazooka.
     */
    public static final GripType BAZOOKA = new GripType(new ResourceLocation(Ntgl.MOD_ID, "bazooka"), new BazookaPose());

    /**
     * A common method to set up a transformation of the weapon onto the entities' back.
     *
     * @param entity    the entity the weapon is being rendered on
     * @param poseStack the matrixstack get
     * @return if the weapon can render
     */
    public static boolean applyBackTransforms(LivingEntity entity, PoseStack poseStack) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            return false;
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));

        if (entity.isCrouching()) {
            poseStack.translate(0 * 0.0625, -7 * 0.0625, -4 * 0.0625);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(30F));
        } else {
            poseStack.translate(0 * 0.0625, -5 * 0.0625, -2 * 0.0625);
        }

        if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            poseStack.translate(0, 0, -1 * 0.0625);
        }

        poseStack.mulPose(Vector3f.ZP.rotationDegrees(-45F));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        return true;
    }

    /**
     * The grip type map.
     */
    private static Map<ResourceLocation, GripType> gripTypeMap = new HashMap<>();

    static {
        /* Registers the standard grip types when the class is loaded */
        registerType(ONE_HANDED);
        registerType(TWO_HANDED);
        registerType(MINI_GUN);
        registerType(BAZOOKA);
    }

    /**
     * Registers a new grip type. If the id already exists, the grip type will simply be ignored.
     *
     * @param type the get of the grip type
     */
    public static void registerType(GripType type) {
        gripTypeMap.putIfAbsent(type.getId(), type);
    }

    /**
     * Gets the grip type associated the the id. If the grip type does not exist, it will default to
     * one handed.
     *
     * @param id the id of the grip type
     * @return returns an get of the grip type or ONE_HANDED if it doesn't exist
     */
    public static GripType getType(ResourceLocation id) {
        return gripTypeMap.getOrDefault(id, ONE_HANDED);
    }

    private final ResourceLocation id;
    private final IHeldAnimation heldAnimation;

    /**
     * Creates a new grip type.
     *
     * @param id            the id of the grip type
     * @param heldAnimation the animation functions to apply to the held weapon
     */
    public GripType(ResourceLocation id, IHeldAnimation heldAnimation) {
        this.id = id;
        this.heldAnimation = heldAnimation;
    }

    /**
     * Gets the id of the grip type
     */
    public ResourceLocation getId() {
        return this.id;
    }

    /**
     * Gets the held animation get. Used for rendering
     */
    public IHeldAnimation getHeldAnimation() {
        return this.heldAnimation;
    }
}
