package com.nukateam.ntgl.client.data.handler;

import com.ibm.icu.impl.Pair;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.interfaces.CurrentFpsGetter;
import com.nukateam.ntgl.common.event.GunFireEvent;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.helpers.PlayerReviveHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.data.util.GunModifierHelper.canRenderInOffhand;
import static net.minecraftforge.event.TickEvent.Type.RENDER;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler {
    private static ShootingHandler instance;

    public static final String COOLDOWN = "Cooldown";
    public static float shootMsGap = 0F;

    private final HashMap<Pair<HumanoidArm, LivingEntity>, Float> entityShootGaps = new HashMap<>();
//    private int fireTimer;

    public static ShootingHandler get() {
        if (instance == null) {
            instance = new ShootingHandler();
        }
        return instance;
    }

    private boolean shooting;

    public boolean isShooting() {
        return shooting;
    }

    private ShootingHandler() {
    }

    private boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getOverlay() != null)
            return false;
        if (mc.screen != null)
            return false;
        if (!mc.mouseHandler.isMouseGrabbed())
            return false;
        return mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled())
            return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || PlayerReviveHelper.isBleeding(player))
            return;

        if (event.isAttack()) {
            var heldItem = player.getMainHandItem();

            if (heldItem.getItem() instanceof GunItem gunItem) {
                setupShootingData(heldItem, gunItem, HumanoidArm.RIGHT);
                handleGunInput(event, heldItem, gunItem);
            }
        } else if (event.isUseItem()) {
            var mainHandItem = player.getMainHandItem();
            var offhandItem = player.getOffhandItem();

            if (offhandItem.getItem() instanceof GunItem gunItem && canRenderInOffhand(player)) {
                setupShootingData(offhandItem, gunItem, HumanoidArm.LEFT);
                handleGunInput(event, offhandItem, gunItem);
                return;
            }

            if (mainHandItem.getItem() instanceof GunItem gunItem) {
                if (event.getHand() == InteractionHand.OFF_HAND) {
                    // Allow shields to be used if weapon is one-handed
                    if (offhandItem.getItem() == Items.SHIELD) {
                        var modifiedGun = gunItem.getModifiedGun(mainHandItem);
                        if (modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED) {
                            return;
                        }
                    }
                    event.setCanceled(true);
                    event.setSwingHand(false);
                    return;
                }
                if (AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock()) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

    private void setupShootingData(ItemStack stack, GunItem gunItem, HumanoidArm arm) {
        var data = shootingData.get(arm);
        var gun = gunItem.getModifiedGun(stack);
        data.fireTimer = gun.getGeneral().getFireTimer();
        data.gun = gunItem;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleGunInput(InputEvent.InteractionKeyMappingTriggered event, ItemStack heldItem, GunItem gunItem) {
//        var mc = Minecraft.getInstance();
        event.setSwingHand(false);
        event.setCanceled(true);
//        this.fire(mc.player, heldItem);
//        var gun = gunItem.getModifiedGun(heldItem);

//        if (!gun.getGeneral().isAuto()) {
//            if(event.isAttack())
//                mc.options.keyAttack.setDown(false);
//            else if (event.isUseItem())
//                mc.options.keyUse.setDown(false);
//        }
    }

    @SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START)
            return;

        if (!this.isInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            // CHECK HERE: Reduce by 1F in each tick until it is less than 0F
//            shootTickGapLeft -= shootTickGapLeft > 0F ? 1F : 0F;
            reduceGaps();

            var mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof GunItem && (Gun.hasAmmo(mainHandItem) || player.isCreative())) {
                // Update #shooting state if it has changed
//                final boolean shooting = Keys.PULL_TRIGGER.isDown() && GunRenderingHandler.get().sprintTransition == 0;
                var shooting = mc.options.keyAttack.isDown();
                if (shooting ^ this.shooting) {
                    this.shooting = shooting;
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(shooting));
                }
            } else if (this.shooting) {
                this.shooting = false;
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
            }
        } else {
            this.shooting = false;
        }
    }

    private void reduceGaps(){
        entityShootGaps.forEach((key, val) -> {
            if(val > 0) val--;
            entityShootGaps.put(key, val);
        } );
    }

    private final Map<HumanoidArm, ShootingData> shootingData = Map.of(
            HumanoidArm.RIGHT, new ShootingData(0, null),
            HumanoidArm.LEFT, new ShootingData(0, null)
    );

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (!isInGame()) return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || PlayerReviveHelper.isBleeding(player)) return;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        if (mainHandItem.getItem() instanceof GunItem gunItem){
           if(mc.options.keyAttack.isDown())
               handleAutoFire(player, mainHandItem, gunItem, HumanoidArm.RIGHT);
//           else setupShootingData(mainHandItem, gunItem, HumanoidArm.RIGHT);
        }

        if (offhandItem.getItem() instanceof GunItem gunItem && canRenderInOffhand(player)){
            if(mc.options.keyUse.isDown())
                handleAutoFire(player, offhandItem, gunItem, HumanoidArm.LEFT);
//            else setupShootingData(mainHandItem, gunItem, HumanoidArm.LEFT);
        }
    }

    private void handleAutoFire(LocalPlayer player, ItemStack heldItem, GunItem gunItem, HumanoidArm arm) {
        var mc = Minecraft.getInstance();
        var gun = gunItem.getModifiedGun(heldItem);
        var key = arm == HumanoidArm.RIGHT ? mc.options.keyAttack : mc.options.keyUse;
        var data = shootingData.get(arm);

//        if (data.fireTimer == 0 && gun.getGeneral().getFireTimer() != 0) {
//            data.fireTimer = gun.getGeneral().getFireTimer();
//        }
        if (gun.getGeneral().getFireTimer() != 0) {
            var isOnCooldown = ShootingHandler.get().isOnCooldown(player, arm);

            if (data.fireTimer > 0 && !isOnCooldown) {
                if (data.fireTimer == gun.getGeneral().getFireTimer() - 2) {
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
                }
                data.fireTimer--;
            } else {
                // Execute after preFire timer ends
                this.fire(player, heldItem);
//                    if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO || gun.getGeneral().getFireMode() == FireMode.PULSE) {
                if (gun.getGeneral().getFireTimer() > 0) {
                    if(gun.getGeneral().getFireMode() != FireMode.AUTOMATIC)
                        key.setDown(false);
//                    data.fireTimer = gun.getGeneral().getFireTimer();
                }
            }
        } else {
            this.fire(player, heldItem);
            if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO) {
                key.setDown(false);
            }
        }

//        if (gun.getGeneral().isAuto()) {
//            this.fire(player, heldItem);
//        }
    }

//    @SubscribeEvent
//    public void onPostClientTick2(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.END)
//            return;
//
//        if (!isInGame())
//            return;
//
//        var mc = Minecraft.getInstance();
//        var player = mc.player;
//
//        if (player != null) {
//            if (PlayerReviveHelper.isBleeding(player))
//                return;
//
//            ItemStack heldItem = player.getMainHandItem();
//            if (heldItem.getItem() instanceof GunItem) {
//                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
//                if (!KeyBinds.getShootMapping().isDown() && gun.getGeneral().getFireTimer() != 0) {
//                    fireTimer = gun.getGeneral().getFireTimer();
//                }
//                if (KeyBinds.getShootMapping().isDown()) {
//                    if (gun.getGeneral().getFireTimer() != 0) {
//                        ItemCooldowns tracker = player.getCooldowns();
//                        if (fireTimer > 0 && !tracker.isOnCooldown(heldItem.getItem())) {
//                            if (fireTimer == gun.getGeneral().getFireTimer() - 2) {
//                                PacketHandler.getPlayChannel().sendToServer(new C2SMessagePreFireSound(player));
//                            }
//                            // If the player is in water, reduce the preFiring in half
//                            if (player.isUnderWater()) {
//                                fireTimer--;
//                            }
//                            fireTimer--;
//                        } else {
//                            // Execute after preFire timer ends
//                            this.fire(player, heldItem);
//                            if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO || gun.getGeneral().getFireMode() == FireMode.PULSE) {
//                                mc.options.keyAttack.setDown(false);
//                                fireTimer = gun.getGeneral().getFireTimer();
//                            }
//                        }
//                    } else {
//                        this.fire(player, heldItem);
//                        if (gun.getGeneral().getFireMode() == FireMode.SEMI_AUTO) {
//                            mc.options.keyAttack.setDown(false);
//                        }
//                    }
//                }
//            }
//        }
//    }

    public static ArrayList<ItemStack> gunCooldown = new ArrayList<>();

    public static int getCooldown(ItemStack itemStack) {
        var tag =  itemStack.getOrCreateTag();
        return tag.getInt(COOLDOWN);
    }

    private static float visualCooldownMultiplier() {
        int fps = ((CurrentFpsGetter) Minecraft.getInstance()).getCurrentFps();
        if (fps < 11)
            return 8f;
        else if (fps < 21)
            return 6.25f;
        else if (fps < 31)
            return 1.25f;
        else if (fps < 61)
            return 0.95f;
        else if (fps < 121)
            return 0.625f;
        else if (fps < 181)
            return 0.425f;
        else if (fps < 201)
            return 0.35f;
        else
            return 0.25f;
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderTickLow(TickEvent.RenderTickEvent evt) {
        if (!evt.type.equals(RENDER) || evt.phase.equals(TickEvent.Phase.START))
            return;
        
        if (shootMsGap > 0F) {
            shootMsGap -= evt.renderTickTime * visualCooldownMultiplier();
        } else if (shootMsGap < -0.05F)
            shootMsGap = 0F;
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void renderTick(TickEvent.RenderTickEvent evt) {
//        // Upper is to handle rendering, bellow is handling animation calls and burst tracking
//
//        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.isAlive() || Minecraft.getInstance().player.getMainHandItem().getItem() instanceof GunItem)
//            return;
//        GunAnimationController controller = GunAnimationController.fromItem(Minecraft.getInstance().player.getMainHandItem().getItem());
//        if (controller == null)
//            return;
//        else if (controller.isAnimationRunning() && (shootMsGap < 0F && this.burstTracker != 0)) {
//            if (controller.isAnimationRunning(GunAnimationController.AnimationLabel.PUMP) || controller.isAnimationRunning(GunAnimationController.AnimationLabel.PULL_BOLT))
//                return;
//            if (Config.CLIENT.controls.burstPress.get())
//                this.burstTracker = 0;
//            this.clickUp = true;
//        }
//    }

//    public int burstTracker = 0;

    public boolean isOnCooldown(LivingEntity entity, HumanoidArm arm){
        return getCooldown(entity, arm) > 0;
    }

    public float getCooldown(LivingEntity entity, HumanoidArm arm) {
        return entityShootGaps.getOrDefault(Pair.of(arm, entity), 0f);
    }

    public boolean isShooting(LivingEntity entity, HumanoidArm arm){
        var value = entityShootGaps.get(Pair.of(arm, entity));
        if (value != null) return value > 0;
        return false;
    }

    public static float calcShootTickGap(int rpm) {
        float shootTickGap = 60F / rpm * 20F;
        return shootTickGap;
    }

    public void fire(LivingEntity shooter, ItemStack heldItem) {
        if (!(heldItem.getItem() instanceof GunItem)) return;
        if (!Gun.hasAmmo(heldItem)) return;
        if (!Gun.hasAmmo(heldItem) && shooter instanceof Player player && !player.isCreative()) return;
        if (shooter.isSpectator()) return;

        // CHECK HERE: Restrict the fire rate

        var isMainHand = shooter.getMainHandItem() == heldItem;
        var hand = isMainHand ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var shootGap = entityShootGaps.getOrDefault(Pair.of(hand, shooter), 0f);

        if (shootGap <= 0F) {
            var gunItem = (GunItem) heldItem.getItem();
            var modifiedGun = gunItem.getModifiedGun(heldItem);

            if (MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(shooter, heldItem)))
                return;

            // CHECK HERE: Change this to test different rpm settings.
            // TODO: Test serverside, possible issues 0.3.4-alpha
            final float rpm = modifiedGun.getGeneral().getRate(); // Rounds per sec. Should come from gun properties in the end.
            shootGap += rpm;
            entityShootGaps.put(Pair.of(hand, shooter), shootGap);
            shootMsGap = calcShootTickGap((int) rpm);
            RecoilHandler.get().lastRandPitch = RecoilHandler.get().lastRandPitch;
            RecoilHandler.get().lastRandYaw = RecoilHandler.get().lastRandYaw;
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(shooter.getId(), shooter.getViewYRot(1),
                            shooter.getViewXRot(1),
                            RecoilHandler.get().lastRandPitch, RecoilHandler.get().lastRandYaw, isMainHand));

//            if (Config.CLIENT.controls.burstPress.get()) this.burstTracker--;
//            else this.burstTracker++;
            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(shooter, heldItem));
        }
    }

//    private boolean magError(Player player, ItemStack heldItem) {
//        int[] extraAmmo = ((GunItem) heldItem.getItem()).getGun().getReloads().getMaxAdditionalAmmoPerOC();
//        int magMode = GunModifierHelper.getAmmoCapacity(heldItem);
//        if (magMode < 0) {
//            if (heldItem.getItem() instanceof GunItem && heldItem.getTag().getInt("AmmoCount") - 1 > ((GunItem) heldItem.getItem()).getGun().getReloads().getMaxAmmo()) {
//                return true;
//            }
//        } else {
//            if (heldItem.getItem() instanceof GunItem && heldItem.getTag().getInt("AmmoCount") - 1 > ((GunItem) heldItem.getItem()).getGun().getReloads().getMaxAmmo() + extraAmmo[magMode]) {
//                return true;
//            }
//        }
//        return false;
//    }
}
