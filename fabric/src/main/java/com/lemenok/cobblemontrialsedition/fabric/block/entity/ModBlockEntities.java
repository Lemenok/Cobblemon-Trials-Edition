package com.lemenok.cobblemontrialsedition.fabric.block.entity;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.fabric.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<CobblemonTrialSpawnerEntity> COBBLEMON_TRIAL_SPAWNER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID,"cobblemon_trial_spawner"),
                    BlockEntityType.Builder.of(CobblemonTrialSpawnerEntity::new, ModBlocks.COBBLEMON_TRIAL_SPAWNER).build(null));

    public static void registerBlockEntities() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Block Entities for " + CobblemonTrialsEditionFabric.MODID);
    }
}