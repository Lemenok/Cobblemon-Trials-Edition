package com.lemenok.cobblemontrialsedition.builder;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuildSpawner {
    public static CobblemonTrialSpawnerEntity create(RegistryAccess registryAccess, BlockPos blockPosition, IBlockBuilder blockProcessor) {

        SpawnerProperties newSpawnerProperties = blockProcessor.getSpawnerProperties();

        CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig;
        CobblemonTrialSpawnerConfig cobblemonTrialSpawnerOminousConfig;

        cobblemonTrialSpawnerConfig = new CobblemonTrialSpawnerConfig(
                newSpawnerProperties.spawnRange(),
                newSpawnerProperties.totalNumberOfPokemonPerTrial(),
                newSpawnerProperties.maximumNumberOfSimultaneousPokemon(),
                newSpawnerProperties.totalNumberOfPokemonPerTrial(),
                newSpawnerProperties.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                newSpawnerProperties.ticksBetweenSpawnAttempts(),
                newSpawnerProperties.ominousSpawnerAttacksEnabled(),
                newSpawnerProperties.getListOfPokemonToSpawn(registryAccess, false),
                newSpawnerProperties.getLootTables(blockProcessor.getStructureBlockInfo(), false),
                BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
        );
        cobblemonTrialSpawnerOminousConfig = new CobblemonTrialSpawnerConfig(
                newSpawnerProperties.spawnRange(),
                newSpawnerProperties.totalNumberOfPokemonPerTrial(),
                newSpawnerProperties.maximumNumberOfSimultaneousPokemon(),
                newSpawnerProperties.totalNumberOfPokemonPerTrial(),
                newSpawnerProperties.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                newSpawnerProperties.ticksBetweenSpawnAttempts(),
                newSpawnerProperties.ominousSpawnerAttacksEnabled(),
                newSpawnerProperties.getListOfPokemonToSpawn(registryAccess, true),
                newSpawnerProperties.getLootTables(blockProcessor.getStructureBlockInfo(), true),
                BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
        );

        CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(
                blockPosition, Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState());

        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerConfig, false);
        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerOminousConfig, true);
        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setTargetCooldownLength(newSpawnerProperties.spawnerCooldown());
        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setRequiredPlayerRange(newSpawnerProperties.playerDetectionRange());
        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner(), RandomSource.create());
        cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().markUpdated();
        cobblemonTrialSpawnerEntity.markUpdated();

        return cobblemonTrialSpawnerEntity;
    }
}
