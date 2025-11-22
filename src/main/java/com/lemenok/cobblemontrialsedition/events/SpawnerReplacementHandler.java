package com.lemenok.cobblemontrialsedition.events;

import com.cobblemon.mod.common.pokemon.*;
import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.Config;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {

        if(!Config.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS.get())
            return;

        Level level = (Level) event.getLevel();

        // Verify that the events are chunk events.
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()) return;
        
        List<BlockEntity> listOfBlockEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfBlockEntities.add(blockEntity);
            }
        }

        if(listOfBlockEntities.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        List<StructureProperties> listOfStructuresToModify = getListOfStructuresToModify(level);

        for (BlockEntity blockEntity : listOfBlockEntities) {
            try {
                BlockPos blockEntityPosition = blockEntity.getBlockPos();

                if (!serverLevel.isLoaded(blockEntityPosition)) continue;

                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);
                //getListOfFeaturesToModify(chunk);

                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                if (allStructuresAtPosition.isEmpty() && Config.REPLACE_SPAWNERS_OUTSIDE_OF_STRUCTURES_WITH_COBBLEMON_SPAWNERS.get()) {

                    /*CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get().defaultBlockState());
                    cobblemonTrialSpawnerEntity.loadWithComponents(BuildPokemonForSpawn(serverLevel, blockEntityPosition), serverLevel.registryAccess());
                    cobblemonTrialSpawnerEntity.setChanged();

                    chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
                    // ServerLevel set required to properly allow the Trial Spawner to work.
                    serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);*/
                    //LOGGER.info("Replaced Spawner at Location '{}'", blockEntityPosition);
                    break;
                }
                // Check to see if the Structure is on the CustomSpawner List
                else if(Config.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS.get()) {
                    for (Structure structure: allStructuresAtPosition.keySet()){
                        ResourceLocation resourceAtPosition = structureRegistry.getKey(structure);

                        // Check if Structure Exists to have its spawners swapped.
                        List<SpawnerProperties> spawnerPropertiesForStructure = new ArrayList<>();

                        for(StructureProperties properties: listOfStructuresToModify){
                            spawnerPropertiesForStructure = properties.getSpawnerPropertiesIfResourceLocationMatches(resourceAtPosition);
                            if(spawnerPropertiesForStructure != null)
                                break;
                        }

                        if(spawnerPropertiesForStructure != null) {
                            EntityType spawnerEntityType = null;

                            // Grab Entity in spawner to specify which spawner to replace.
                            if(blockEntity instanceof SpawnerBlockEntity){
                                spawnerEntityType = Objects.requireNonNull(((SpawnerBlockEntity) blockEntity).getSpawner().getOrCreateDisplayEntity(level, blockEntity.getBlockPos())).getType();
                            }
                            if (blockEntity instanceof TrialSpawnerBlockEntity){
                                spawnerEntityType = getEntityTypeFromTrialSpawner(Objects.requireNonNull(((TrialSpawnerBlockEntity) blockEntity).getTrialSpawner()));
                            }

                            SpawnerProperties newSpawner = null;
                            // Grab the spawner settings that match the blockEntity and Entity in the spawner to be replaced. (e.g. Replace Blaze Trial Spawner with specific spawner)
                            for(SpawnerProperties spawner: spawnerPropertiesForStructure) {
                                if(spawner.doesSpawnerSettingsContainEntityType(level, spawnerEntityType, blockEntity)) {
                                    newSpawner = spawner;
                                    break;
                                }
                            }

                            if(newSpawner != null) {
                                // Setup all configuration for the spawner.
                                CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig;
                                CobblemonTrialSpawnerConfig cobblemonTrialSpawnerOminousConfig;

                                cobblemonTrialSpawnerConfig = new CobblemonTrialSpawnerConfig(
                                        newSpawner.spawnRange(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemon(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                                        newSpawner.ticksBetweenSpawnAttempts(),
                                        newSpawner.ominousSpawnerAttacksEnabled(),
                                        newSpawner.getListOfPokemonToSpawn(serverLevel, false),
                                        newSpawner.getLootTables(blockEntity, false),
                                        BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
                                );
                                cobblemonTrialSpawnerOminousConfig = new CobblemonTrialSpawnerConfig(
                                        newSpawner.spawnRange(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemon(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                                        newSpawner.ticksBetweenSpawnAttempts(),
                                        newSpawner.ominousSpawnerAttacksEnabled(),
                                        newSpawner.getListOfPokemonToSpawn(serverLevel, true),
                                        newSpawner.getLootTables(blockEntity, true),
                                        BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
                                );

                                CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(
                                        blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get().defaultBlockState());

                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerConfig, false);
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerOminousConfig, true);
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setTargetCooldownLength(newSpawner.spawnerCooldown());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setRequiredPlayerRange(newSpawner.playerDetectionRange());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner(),RandomSource.create());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().markUpdated();
                                cobblemonTrialSpawnerEntity.markUpdated();

                                chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
                                serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);
                                LOGGER.info("Replaced Entity: '{}' Spawner at Location '{}' in Structure: '{}'", spawnerEntityType, blockEntityPosition, resourceAtPosition);
                                break;
                            }
                        }

                        // Structure is not listed to have its spawner replaced.
                    }
                }
                // If there are still structures around the spawner this means that the spawner is in a structure
                // that the user has defined they WANT to leave the default spawner. So we do nothing.

            } catch (Exception ex) {
                LOGGER.error(ex);
                throw ex;
            }
        }
    }

    private static List<StructureProperties> getListOfStructuresToModify(Level level) {
        var ctsstructureRegistry = level.registryAccess().registryOrThrow(CobblemonTrialsEdition.ClientModEvents.COBBLEMON_TRIALS_STRUCTURE_REGISTRY);
        Set<String> pathsInNamespace = ctsstructureRegistry.keySet().stream()
                .filter(resourceLocation -> resourceLocation.getNamespace().equals(CobblemonTrialsEdition.MODID))
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());

        List<StructureProperties> listOfStructuresToModify = new ArrayList<>();
        for(String path: pathsInNamespace){
            listOfStructuresToModify.add(ctsstructureRegistry.get(ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, path)));
        }

        return listOfStructuresToModify;
    }

    private static void getListOfFeaturesToModify(LevelChunk chunk){
        Map<Structure, StructureStart> starts = chunk.getAllStarts();

        for (StructureStart start : starts.values()) {
            if (!start.isValid()) continue;

            // Option A: check the feature directly if you can reference the dungeon feature
            // if (start.getFeature() == Feature.DUNGEON_FEATURE / StructureFeature.DUNGEON) { ... }

            // Option B: inspect pieces for a dungeon-like piece id or class
            for (StructurePiece piece : start.getPieces()) {
                // safe string check for piece id (works across mappings)
                String id = piece.getType().toString().toLowerCase(); // or use piece.getClass().getSimpleName()
                if (id.contains("dungeon") || id.contains("monster_room") || id.contains("dungeon_piece")) {
                    // found a dungeon piece in this chunk
                    LOGGER.info("Monster Room found");
                    return;
                }
            }
        }
    }

    private EntityType getEntityTypeFromTrialSpawner(TrialSpawner trialSpawner) {
        String entityId = trialSpawner.getConfig().spawnPotentialsDefinition().getRandomValue(RandomSource.create()).get().entityToSpawn().getString("id");
        ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);

        return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
    }
}
