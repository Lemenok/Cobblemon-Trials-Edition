package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ReplaceSpawners {

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    public static void Process(ServerLevel serverLevel, LevelChunk chunk, List<BlockEntity> listOfBlockEntities, StructureManager structureManager, Level level, Registry<Structure> structureRegistry) {

        for (BlockEntity blockEntity : listOfBlockEntities) {
            try {
                BlockPos blockEntityPosition = blockEntity.getBlockPos();

                if (!serverLevel.isLoaded(blockEntityPosition)) continue;

                if(ShouldSpawnerBeReplaced(blockEntity))
                    continue;

                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

                if(!allStructuresAtPosition.isEmpty() && Services.PLATFORM.getCommonConfig().REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS) {

                    List<StructureProperties> listOfStructuresToModify = getStructuresToModify(level, Services.PLATFORM.getCobblemonTrialsStructureRegistry());

                    EntityType spawnerEntityType = getEntityType(level, blockEntity);

                    ReplaceSpawnerWithStructureConfig(serverLevel, chunk, level, structureRegistry, blockEntity, allStructuresAtPosition, listOfStructuresToModify, spawnerEntityType, blockEntityPosition);
                }

                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                if (allStructuresAtPosition.isEmpty() && Services.PLATFORM.getCommonConfig().REPLACE_SPAWNERS_IN_FEATURES) {

                    EntityType spawnerEntityType = getEntityType(level, blockEntity);

                    if (replaceWithDefaultSpawner(serverLevel, chunk, level, blockEntity, spawnerEntityType, blockEntityPosition, Services.PLATFORM.getCobblemonTrialsFeaturesRegistry()))
                        break;
                }

            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
    }

    private static boolean ShouldSpawnerBeReplaced(BlockEntity blockEntity) {
        if(blockEntity instanceof SpawnerBlockEntity && Services.PLATFORM.getCommonConfig().REPLACE_MOB_SPAWNERS_BASED_ON_PERCENTAGE){
            if(Services.PLATFORM.getCommonConfig().MOB_SPAWNER_REPLACEMENT_PERCENTAGE <= Math.random()) {
                if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                    LOGGER.info("Skipped replacement of Mob Spawner at: {}", blockEntity.getBlockPos());
                return true;
            }
        }

        if(blockEntity instanceof TrialSpawnerBlockEntity && Services.PLATFORM.getCommonConfig().REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE){
            if(Services.PLATFORM.getCommonConfig().TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE <= Math.random()){
                if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                    LOGGER.info("Skipped replacement of Trial Spawner at: {}", blockEntity.getBlockPos());
                return true;
            }
        }
        if(blockEntity instanceof SculkShriekerBlockEntity && Services.PLATFORM.getCommonConfig().REPLACE_SHRIEKERS_BASED_ON_PERCENTAGE){
            if(Services.PLATFORM.getCommonConfig().SHRIEKER_REPLACEMENT_PERCENTAGE <= Math.random()) {
                if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                    LOGGER.info("Skipped replacement of Sculk Shrieker at: {}", blockEntity.getBlockPos());
                return true;
            }
        }

        return false;
    }

    private static void ReplaceSpawnerWithStructureConfig(ServerLevel serverLevel, LevelChunk chunk, Level level, Registry<Structure> structureRegistry,
                                                          BlockEntity blockEntity, Map<Structure, LongSet> allStructuresAtPosition,
                                                          List<StructureProperties> listOfStructuresToModify, EntityType spawnerEntityType,
                                                          BlockPos blockEntityPosition) throws Exception {
        for (Structure structure: allStructuresAtPosition.keySet()){
            Holder<Structure> resourceAtPosition = structureRegistry.wrapAsHolder(structure);

            // Check if Structure Exists to have its spawners swapped.
            List<SpawnerProperties> spawnerPropertiesForStructure = new ArrayList<>();
            for(StructureProperties properties: listOfStructuresToModify){
                //spawnerPropertiesForStructure = properties.getSpawnerPropertiesIfResourceLocationMatches(resourceAtPosition);
                // Break early if found.
                if(spawnerPropertiesForStructure != null)
                    break;
            }



            if(spawnerPropertiesForStructure != null) {
                if (replaceSpawner(serverLevel, chunk, level, spawnerEntityType, blockEntity, spawnerPropertiesForStructure, blockEntityPosition)) {
                    if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                        LOGGER.info("Replaced: '{}' Spawner at Location '{}', Structure: '{}'.", spawnerEntityType, blockEntityPosition, resourceAtPosition);
                    return;
                }
            }

            // This uses the Default json files under "defaults"
            // If I can ever figure out how to do StructureProcessors this would not be necessary.
            if(Services.PLATFORM.getCommonConfig().REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS){
                replaceWithDefaultSpawner(serverLevel, chunk, level, blockEntity, spawnerEntityType,
                        blockEntityPosition, Services.PLATFORM.getCobblemonTrialsDefaultStructureRegistry());
            }
        }
    }

    private static boolean replaceWithDefaultSpawner(ServerLevel serverLevel, LevelChunk chunk, Level level, BlockEntity blockEntity,
                                                     EntityType spawnerEntityType, BlockPos blockEntityPosition,
                                                     ResourceKey<Registry<StructureProperties>> registryResourceKey) throws Exception {

        List<SpawnerProperties> defaultSpawnerProperties = getStructuresToModify(level, registryResourceKey).getFirst().spawnerProperties();

        if(defaultSpawnerProperties == null)
            throw new Exception("No Default Spawners file detected.");

        if (replaceSpawner(serverLevel, chunk, level, spawnerEntityType, blockEntity,
                defaultSpawnerProperties,
                blockEntityPosition)) {
            if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS) {
                LOGGER.info("Replaced: '{}' Spawner at Location '{}', Default.", spawnerEntityType, blockEntityPosition);
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
                    blockEntityPosition, Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState());

            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerConfig, false);
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerOminousConfig, true);
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setTargetCooldownLength(newSpawnerProperties.spawnerCooldown());
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setRequiredPlayerRange(newSpawnerProperties.playerDetectionRange());
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner(), RandomSource.create(), serverLevel);
            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().markUpdated();
            cobblemonTrialSpawnerEntity.markUpdated();

            chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
            chunk.removeBlockEntity(blockEntityPosition);
            chunk.addAndRegisterBlockEntity(cobblemonTrialSpawnerEntity);
            serverLevel.getChunkSource().blockChanged(blockEntityPosition);

            //serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);

            return true;
        }
        return false;
    }

    private static List<StructureProperties> getStructuresToModify(Level level, ResourceKey<Registry<StructureProperties>> structureRegistry) {
        var ctsstructureRegistry = level.registryAccess().registryOrThrow(structureRegistry);
        Set<String> pathsInNamespace = ctsstructureRegistry.keySet().stream()
                .filter(resourceLocation -> resourceLocation.getNamespace().equals(Services.PLATFORM.getModID()))
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());

        List<StructureProperties> listOfStructuresToModify = new ArrayList<>();
        for(String path: pathsInNamespace){
            listOfStructuresToModify.add(ctsstructureRegistry.get(ResourceLocation.fromNamespaceAndPath(Services.PLATFORM.getModID(), path)));
        }

        return listOfStructuresToModify;
    }

    private static @Nullable EntityType getEntityType(Level level, BlockEntity blockEntity) {
        EntityType spawnerEntityType = null;

        try {
            // Grab Entity in spawner to specify which spawner to replace.
            if(blockEntity instanceof SpawnerBlockEntity spawner){
                CompoundTag nbt = spawner.saveWithId(level.registryAccess());
                if (!hasEntityData(nbt)) {
                    LOGGER.info("Empty spawner at {}, defaulting to zombie", blockEntity.getBlockPos());
                    return EntityType.ZOMBIE; // Return default entity
                }
                var displayEntity = spawner.getSpawner().getOrCreateDisplayEntity(level, blockEntity.getBlockPos());
                if (displayEntity != null) {
                    return displayEntity.getType();
                }
            }

            if(blockEntity instanceof SculkShriekerBlockEntity shrieker){
                spawnerEntityType = EntityType.WARDEN;
            }

            if(blockEntity instanceof TrialSpawnerBlockEntity){
                spawnerEntityType = getEntityTypeFromTrialSpawner(Objects.requireNonNull(((TrialSpawnerBlockEntity) blockEntity).getTrialSpawner()));
            }
        } catch (Exception e) {
            LOGGER.error("Entity extraction failed", e);
        }
    
        return spawnerEntityType;
    }

    private static boolean hasEntityData(CompoundTag nbt) {
        // Check if the spawner has any entity data at all
        if (nbt.contains("SpawnData", CompoundTag.TAG_COMPOUND)) {
            CompoundTag spawnData = nbt.getCompound("SpawnData");

            // Check various possible entity locations
            if (spawnData.contains("entity", CompoundTag.TAG_COMPOUND)) {
                return !spawnData.getCompound("entity").isEmpty();
            }
            if (spawnData.contains("id", CompoundTag.TAG_STRING)) {
                return !spawnData.getString("id").isEmpty();
            }
        }

        if (nbt.contains("SpawnPotentials", CompoundTag.TAG_LIST)) {
            ListTag potentials = nbt.getList("SpawnPotentials", CompoundTag.TAG_COMPOUND);

            for(int i = 0; i < potentials.size(); i++) {
                CompoundTag entry = potentials.getCompound(i);
                CompoundTag data = entry.getCompound("data");
                CompoundTag entity = data.getCompound("entity");

                if (entity.contains("id", Tag.TAG_STRING)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static EntityType getEntityTypeFromTrialSpawner(TrialSpawner trialSpawner) {
        String entityId = trialSpawner.getConfig().spawnPotentialsDefinition().getRandomValue(RandomSource.create()).get().entityToSpawn().getString("id");
        ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);

        return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
    }
}
