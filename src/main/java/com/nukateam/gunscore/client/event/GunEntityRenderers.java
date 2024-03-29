package com.nukateam.gunscore.client.event;

import com.nukateam.gunscore.client.render.entity.*;
import com.nukateam.gunscore.common.foundation.init.Projectiles;
import com.nukateam.gunscore.GunMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Projectiles.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.LASER_PROJECTILE.get(), LaserProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.TESLA_PROJECTILE.get(), TeslaProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(Projectiles.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
    }
}
