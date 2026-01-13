package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ReplaceSpawners {

    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    public static void Process(ServerLevel serverLevel, LevelChunk chunk, List<BlockEntity> listOfBlockEntities, StructureManager structureManager, Level level, Registry<Structure> structureRegistry) {

        Config modConfig = AutoConfig.getConfigHolder(Config.class).getConfig();

        for (BlockEntity blockEntity : listOfBlockEntities) {
            try {
                BlockPos blockEntityPosition = blockEntity.getBlockPos();

                if (!serverLevel.isLoaded(blockEntityPosition)) continue;

                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

                if(!allStructuresAtPosition.isEmpty() && modConfig.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS) {

                    List<StructureProperties> listOfStructuresToModify = getStructuresToModify(level, CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_STRUCTURE_REGISTRY);

                    for (Structure structure: allStructuresAtPosition.keySet()){
                        ResourceLocation resourceAtPosition = structureRegistry.getKey(structure);

                        // Check if Structure Exists to have its spawners swapped.
                        List<SpawnerProperties> spawnerPropertiesForStructure = new ArrayList<>();
                        for(StructureProperties properties: listOfStructuresToModify){
                            spawnerPropertiesForStructure = properties.getSpawnerPropertiesIfResourceLocationMatches(resourceAtPosition);
                            // Break early if found.
                            if(spawnerPropertiesForStructure != null)
                                break;
                        }

                        EntityType spawnerEntityType = getEntityType(level, blockEntity);

                        if(spawnerPropertiesForStructure != null) {
                            if (replaceSpawner(serverLevel, chunk, level, spawnerEntityType, blockEntity, spawnerPropertiesForStructure, blockEntityPosition)) {
                                if(modConfig.ENABLE_DEBUG_LOGS)
                                    LOGGER.info("Replaced: '{}' Spawner at Location '{}', Structure: '{}'.", spawnerEntityType, blockEntityPosition, resourceAtPosition);
                                break;
                            }
                        }

                        // This uses the Default json files under "defaults"
                        // If I can ever figure out how to do StructureProcessors this would not be necessary.
                        if(modConfig.REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS){

                            if (replaceWithDefaultSpawner(serverLevel, chunk, level, blockEntity, spawnerEntityType, blockEntityPosition, modConfig, CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY))
                                break;
                        }
                    }
                }

                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                if (allStructuresAtPosition.isEmpty() && modConfig.REPLACE_SPAWNERS_IN_FEATURES) {

                    EntityType spawnerEntityType = getEntityType(level, blockEntity);
                    if (replaceWithDefaultSpawner(serverLevel, chunk, level, blockEntity, spawnerEntityType, blockEntityPosition, modConfig, CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_FEATURES_REGISTRY))
                        break;
                }

            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
    }

    private static boolean replaceWithDefaultSpawner(ServerLevel serverLevel, LevelChunk chunk, Level level, BlockEntity blockEntity, EntityType spawnerEntityType, BlockPos blockEntityPosition, Config modConfig, ResourceKey<Registry<StructureProperties>> registryResourceKey) throws Exception {
        StructureProperties defaultSpawnerProperties = getStructuresToModify(level, registryResourceKey).getFirst();
        if(defaultSpawnerProperties == null)
            throw new Exception("No Default Spawners file detected.");


        if (replaceSpawner(serverLevel, chunk, level, spawnerEntityType, blockEntity,
                defaultSpawnerProperties.getSpawnerPropertiesIfResourceLocationMatches(defaultSpawnerProperties.structureId()),
                blockEntityPosition)) {
            if(modConfig.ENABLE_DEBUG_LOGS) {
                LOGGER.info("Replaced: '{}' Spawner at Location '{}'", spawnerEntityType, blockEntityPosition);
            }
            return true;
        }
        return false;
    }

    private static boolean replaceSpawner(ServerLevel serverLevel, LevelChunk chunk, Level level, EntityType spawnerEntityType, BlockEntity blockEntity, List<SpawnerProperties> spawnerPropertiesForStructure, BlockPos blockEntityPosition) {
        SpawnerProperties newSpawnerProperties = null;
        // Grab the spawner settings that match the blockEntity and Entity in the spawner to be replaced. (e.g. Replace Blaze Trial Spawner with specific spawner)
        for(SpawnerProperties spawnerProperties : spawnerPropertiesForStructure) {
            if(spawnerProperties.doesSpawnerSettingsContainEntityType(level, spawnerEntityType, blockEntity)) {
                newSpawnerProperties = spawnerProperties;
                break;
            }
        }

        if(newSpawnerProperties != null) {
            // Setup all configuration for the spawner.
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
                    newSpawnerProperties.getListOfPokemonToSpawn(serverLevel, false),
                    newSpawnerProperties.getLootTables(blockEntity, false),
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
                    newSpawnerProperties.getListOfPokemonToSpawn(serverLevel, true),
                    newSpawnerProperties.getLootTables(blockEntity, true),
                    BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
            );

            CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(
                    blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.defaultBlockState());

            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerConfig, false);
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerOminousConfig, true);
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setTargetCooldownLength(newSpawnerProperties.spawnerCooldown());
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setRequiredPlayerRange(newSpawnerProperties.playerDetectionRange());
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner(), RandomSource.create());
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().markUpdated();
            cobblemonTrialSpawnerEntity.markUpdated();

            chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
            chunk.removeBlockEntity(blockEntityPosition);
            chunk.addAndRegisterBlockEntity(cobblemonTrialSpawnerEntity);
            serverLevel.getChunkSource().blockChanged(blockEntityPosition);


            return true;
        }
        return false;
    }

    private static List<StructureProperties> getStructuresToModify(Level level, ResourceKey<Registry<StructureProperties>> structureRegistry) {
        var ctsstructureRegistry = level.registryAccess().registryOrThrow(structureRegistry);
        Set<String> pathsInNamespace = ctsstructureRegistry.keySet().stream()
                .filter(resourceLocation -> resourceLocation.getNamespace().equals(CobblemonTrialsEditionFabric.MODID))
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());

        List<StructureProperties> listOfStructuresToModify = new ArrayList<>();
        for(String path: pathsInNamespace){
            listOfStructuresToModify.add(ctsstructureRegistry.get(ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, path)));
        }

        return listOfStructuresToModify;
    }

    private static @Nullable EntityType getEntityType(Level level, BlockEntity blockEntity) {
        EntityType spawnerEntityType = null;

        // Grab Entity in spawner to specify which spawner to replace.
        if(blockEntity instanceof SpawnerBlockEntity){
            spawnerEntityType = Objects.requireNonNull(((SpawnerBlockEntity) blockEntity).getSpawner().getOrCreateDisplayEntity(level, blockEntity.getBlockPos())).getType();
        }
        if (blockEntity instanceof TrialSpawnerBlockEntity){
            spawnerEntityType = getEntityTypeFromTrialSpawner(Objects.requireNonNull(((TrialSpawnerBlockEntity) blockEntity).getTrialSpawner()));
        }
        return spawnerEntityType;
    }

    private static EntityType getEntityTypeFromTrialSpawner(TrialSpawner trialSpawner) {
        String entityId = trialSpawner.getConfig().spawnPotentialsDefinition().getRandomValue(RandomSource.create()).get().entityToSpawn().getString("id");
        ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);

        return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
    }
}
