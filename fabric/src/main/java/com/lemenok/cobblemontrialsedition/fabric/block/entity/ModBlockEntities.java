package com.lemenok.cobblemontrialsedition.fabric.block.entity;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.fabric.block.ModBlocks;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<CobblemonTrialSpawnerEntity> COBBLEMON_TRIAL_SPAWNER =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID,"cobblemon_trial_spawner"),
                    BlockEntityType.Builder.of(CobblemonTrialSpawnerEntity::new,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_VANILLA,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_MAGMA,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_DUNGEON,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_RELIC,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_END,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_PRISMARINE,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_FORTRESS,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_LABORATORY,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_CREATE).build(null));

    public static void registerBlockEntities() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Block Entities for " + CobblemonTrialsEditionFabric.MODID);
    }
}