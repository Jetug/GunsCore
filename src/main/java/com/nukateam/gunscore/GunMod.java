package com.nukateam.gunscore;

import com.mojang.logging.LogUtils;
import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import com.nukateam.gunscore.client.ClientHandler;
import com.nukateam.gunscore.client.MetaLoader;
import com.nukateam.gunscore.client.data.handler.CrosshairHandler;
import com.nukateam.gunscore.common.base.utils.BoundingBoxManager;
import com.nukateam.gunscore.common.base.utils.ProjectileManager;
import com.nukateam.gunscore.common.data.datagen.*;
import com.nukateam.gunscore.common.foundation.ModBlocks;
import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.gunscore.common.foundation.crafting.ModRecipeType;
import com.nukateam.gunscore.common.foundation.crafting.WorkbenchIngredient;
import com.nukateam.gunscore.common.foundation.entity.*;
import com.nukateam.gunscore.common.foundation.init.*;
import com.nukateam.gunscore.common.network.PacketHandler;
import mod.azure.azurelib.AzureLib;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.slf4j.Logger;

import static com.nukateam.example.common.registery.ModGuns.*;

@Mod(GunMod.MOD_ID)
public class GunMod {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "gunscore";
    public static final IEventBus MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

//    public GunMod() {
//        AzureLib.initialize();
//        new com.nukateam.guns.GunMod().initGunMod(MOD_EVENT_BUS);
//
//        ModGuns.register(MOD_EVENT_BUS);
//        ModBlocks.register(MOD_EVENT_BUS);
////        ModTileEntities.REGISTER.register(MOD_EVENT_BUS);
//
////        MOD_EVENT_BUS.addListener(this::clientSetup);
//
//        MinecraftForge.EVENT_BUS.register(this);
//
//    }

    public static boolean debugging = false;
    public static boolean controllableLoaded = false;
    public static boolean backpackedLoaded = false;
    public static boolean playerReviveLoaded = false;
//    public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(com.nukateam.gunscore.GunMod.MOD_ID);

    public GunMod() {
        AzureLib.initialize();

        ModGuns.register(MOD_EVENT_BUS);
        ModBlocks.register(MOD_EVENT_BUS);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        //ModBlocks.REGISTER.register(bus);
        ModContainers.REGISTER.register(MOD_EVENT_BUS);
        ModEffects.REGISTER.register(MOD_EVENT_BUS);
        ModEnchantments.REGISTER.register(MOD_EVENT_BUS);
        Projectiles.REGISTER.register(MOD_EVENT_BUS);
//        ModItems.REGISTER.register(MOD_EVENT_BUS);
        ModParticleTypes.REGISTER.register(MOD_EVENT_BUS);
        ModRecipeSerializers.REGISTER.register(MOD_EVENT_BUS);
        ModSounds.REGISTER.register(MOD_EVENT_BUS);
        ModTileEntities.REGISTER.register(MOD_EVENT_BUS);
        MOD_EVENT_BUS.addListener(this::onCommonSetup);
        MOD_EVENT_BUS.addListener(this::onClientSetup);
        MOD_EVENT_BUS.addListener(this::onGatherData);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MOD_EVENT_BUS.addListener(CrosshairHandler::onConfigReload);
            MOD_EVENT_BUS.addListener(ClientHandler::onRegisterReloadListener);
            FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
        });
        controllableLoaded = ModList.get().isLoaded("controllable");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        playerReviveLoaded = ModList.get().isLoaded("playerrevive");

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static boolean isDebugging() {
        return false; //!FMLEnvironment.production;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModRecipeType.init();
            ModSyncedDataKeys.register();
            CraftingHelper.register(new ResourceLocation(MOD_ID, "workbench_ingredient"),
                    WorkbenchIngredient.Serializer.INSTANCE);

            registerProjectiles();

            PacketHandler.init();
            if (Config.COMMON.gameplay.improvedHitboxes.get()) {
                MinecraftForge.EVENT_BUS.register(new BoundingBoxManager());
            }
        });
    }

    private static void registerProjectiles() {
        ProjectileManager.getInstance().registerFactory(GRENADE.get(),
                (worldIn, entity, weapon, item, modifiedGun) -> new GrenadeEntity(Projectiles.GRENADE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(MISSILE.get(),
                (worldIn, entity, weapon, item, modifiedGun) -> new MissileEntity(Projectiles.MISSILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND10MM.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                      new LaserProjectile(Projectiles.LASER_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND45.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                      new TeslaProjectile(Projectiles.TESLA_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));

        ProjectileManager.getInstance().registerFactory(ROUND5MM.get(),
                (worldIn, entity, weapon, item, modifiedGun) ->
                      new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
    }

    private void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        BlockTagGen blockTagGen = new BlockTagGen(generator, existingFileHelper);
        generator.addProvider(new RecipeGen(generator));
        generator.addProvider(new LootTableGen(generator));
        generator.addProvider(blockTagGen);
        generator.addProvider(new ItemTagGen(generator, blockTagGen, existingFileHelper));
        generator.addProvider(new LanguageGen(generator));
        generator.addProvider(new GunGen(generator));
    }
}
