package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.common.foundation.blockentity.WorkbenchBlockEntity;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Ntgl.MOD_ID);

    public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH = register("workbench",
            WorkbenchBlockEntity::new, () -> new Block[]{ ModBlocks.WORKBENCH.get() });

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String id,
                                                                                       BlockEntityType.BlockEntitySupplier<T> factoryIn,
                                                                                       Supplier<Block[]> validBlocksSupplier) {
        return REGISTER.register(id, () -> BlockEntityType.Builder.of(factoryIn, validBlocksSupplier.get()).build(null));
    }
}
