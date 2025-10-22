package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.models.CobblemonTrialSpawner;
import com.lemenok.cobblemontrialsedition.models.CobblemonTrialSpawnerData;
import com.lemenok.cobblemontrialsedition.processors.ConfigProcessor;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

public class SpawnerReplacementHandler {
    // TODO: Create Centralized Logger
    // TODO: Pull data from config
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Level level = (Level) event.getLevel();

        // Verify that the events are chunk events.
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()) return;

        // Collect positions of spawners within the chunk first
        List<BlockPos> spawnersToReplace = new ArrayList<>();
        for (BlockEntity anyBlockEntity : chunk.getBlockEntities().values()) {
            if (anyBlockEntity instanceof SpawnerBlockEntity) {
                spawnersToReplace.add(anyBlockEntity.getBlockPos());
            }
        }

        if (spawnersToReplace.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();

        for (BlockPos blockEntityPosition : spawnersToReplace) {
            try {
                if (!serverLevel.isLoaded(blockEntityPosition)) continue;

                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                if (allStructuresAtPosition.isEmpty()) {

                    CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get().defaultBlockState());

                    SpawnData newSpawnData = new SpawnData(BuildTrialSpawnerNBT(blockEntityPosition), Optional.empty(), Optional.empty());
                    CobblemonTrialSpawnerData trialSpawnerData = new CobblemonTrialSpawnerData(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.of(newSpawnData), Optional.empty());

                    PlayerDetector playerDetector = PlayerDetector.NO_CREATIVE_PLAYERS;
                    PlayerDetector.EntitySelector entitySelector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
                    CobblemonTrialSpawner trialSpawner = new CobblemonTrialSpawner(TrialSpawnerConfig.DEFAULT, TrialSpawnerConfig.DEFAULT,trialSpawnerData, 1200, 14, cobblemonTrialSpawnerEntity,playerDetector,entitySelector);
                    cobblemonTrialSpawnerEntity.setCobblemonTrialSpawner(trialSpawner);

                    /*Pokemon pokemon = new Pokemon();
                    pokemon.initialize();
                    //Species species = new Species();
                    //species.initialize();
                    //species.setName("beedrill");
                    //pokemon.setSpecies(species);
                    PokemonEntity pokemonEntity = new PokemonEntity(level, pokemon, CobblemonEntities.POKEMON);
                    pokemonEntity.setPose(Pose.STANDING);
                    //pokemonEntity.load(BuildTrialSpawnerNBT(blockEntityPosition));


                    newTrialSpawnerBlockEntity.setEntityId(pokemonEntity.getType(), RandomSource.create());*/

                    CompoundTag poseType = new CompoundTag();
                    poseType.putString("PoseType", "WALK");
                    cobblemonTrialSpawnerEntity.loadWithComponents(poseType, serverLevel.registryAccess());

                    //var spawner = newTrialSpawnerBlockEntity.getCobblemonTrialSpawner();
                    //var spawnerData = spawner.getData();

                    cobblemonTrialSpawnerEntity.setChanged();

                    chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
                    // ServerLevel set required to properly allow the Trial Spawner to work.
                    serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);
                    LOGGER.info("Replaced Spawner at Location '{}'", blockEntityPosition);
                    return;
                }
                // Check to see if the Structure is on the CustomSpawner List
                else if (isStructurePresentAt(allStructuresAtPosition)) {
                    chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, Blocks.TRIAL_SPAWNER.defaultBlockState());
                    chunk.setBlockEntity(new TrialSpawnerBlockEntity(blockEntityPosition, Blocks.TRIAL_SPAWNER.defaultBlockState()));
                    LOGGER.info("Replaced Structure Spawner at Location '{}'", blockEntityPosition);
                    return;
                }

                // If there are still structures around the spawner this means that the spawner is in a structure
                // that the user has defined they WANT to leave the default spawner. So we do nothing.

            } catch (Exception ex) {
                LOGGER.error(ex);
                throw ex;
            }
        }
    }

    private boolean isStructurePresentAt(Map<Structure, LongSet> allStructuresAtPosition) {

        for (ResourceLocation resourceLocation : ConfigProcessor.WHITELISTED_STRUCTURES) {
            try {
                for (Structure structure : allStructuresAtPosition.keySet()){
                    // Get the ResourceKey for the given Structure instance.
                    Optional<ResourceKey<StructureType<?>>> optionalStructureKey = BuiltInRegistries.STRUCTURE_TYPE.getResourceKey(structure.type());

                    // If the Structure is in the registry, proceed with the comparison.
                    if (optionalStructureKey.isPresent()) {
                        ResourceKey<StructureType<?>> structureKey = optionalStructureKey.get();
                        ResourceLocation structureLocation = structureKey.location(); // Get the ResourceLocation from the key.

                        if(resourceLocation.equals(structureLocation)) return true;
                    }
                }
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return false;
    }

    public static CompoundTag BuildTrialSpawnerNBT(BlockPos blockEntityPosition) {

        // Build the innermost Pokemon compound
        CompoundTag pokemon = new CompoundTag();

        // Simple string/number/byte/float fields
        pokemon.putByte("Tradeable", (byte)1);
        pokemon.put("PersistentData", new CompoundTag()); // empty compound
        pokemon.putString("Gender", "GENDERLESS");
        pokemon.putInt("HealingTimer", 60);
        pokemon.putInt("Friendship", 140);
        pokemon.putByte("GmaxFactor", (byte)0);
        pokemon.putString("CaughtBall", "cobblemon:poke_ball");
        pokemon.putFloat("ScaleModifier", 1.0f);
        pokemon.putString("FormId", "");
        pokemon.putIntArray("UUID", new int[] { -2017762761, -223001211, -1359438121, 803759701 });
        pokemon.putByte("Shiny", (byte)0);
        pokemon.putString("Species", "cobblemon:beedrill");
        pokemon.putInt("cobblemon:data_version", 1);

        // Ability as a compound
        CompoundTag ability = new CompoundTag();
        ability.putString("AbilityName", "earlybird");
        ability.putInt("AbilityIndex", 1);
        pokemon.put("Ability", ability);

        // Nature
        pokemon.putString("Nature", "cobblemon:calm");

        // BenchedMoves and MoveSet as empty lists
        pokemon.put("BenchedMoves", new ListTag());
        pokemon.put("MoveSet", new ListTag());

        // EVs compound (all zero)
        CompoundTag evs = new CompoundTag();
        evs.putInt("cobblemon:defence", 0);
        evs.putInt("cobblemon:hp", 0);
        evs.putInt("cobblemon:special_defence", 0);
        evs.putInt("cobblemon:special_attack", 0);
        evs.putInt("cobblemon:attack", 0);
        evs.putInt("cobblemon:speed", 0);
        pokemon.put("EVs", evs);

        // Original trainer type
        pokemon.putString("PokemonOriginalTrainerType", "NONE");

        // Health, TeraType, IVs, timers, levels, evolutions, experience
        pokemon.putInt("Health", 13);
        pokemon.putString("TeraType", "cobblemon:bug");

        // IVs compound
        CompoundTag ivs = new CompoundTag();
        ivs.putInt("cobblemon:defence", 8);
        ivs.putInt("cobblemon:hp", 0);
        ivs.putInt("cobblemon:special_defence", 25);
        ivs.putInt("cobblemon:special_attack", 16);
        ivs.putInt("cobblemon:attack", 26);
        ivs.putInt("cobblemon:speed", 27);
        pokemon.put("IVs", ivs);

        pokemon.putInt("FaintedTimer", -1);
        pokemon.putInt("DmaxLevel", 0);

        // Evolutions compound with two empty lists: pending and progress
        CompoundTag evolutions = new CompoundTag();
        evolutions.put("pending", new ListTag());
        evolutions.put("progress", new ListTag());
        pokemon.put("Evolutions", evolutions);

        pokemon.putInt("Experience", 6);
        pokemon.putInt("Level", 2);

        // Build entity compound that wraps id and Pokemon
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "cobblemon:pokemon");
        entity.put("Pokemon", pokemon);
        entity.putString("PoseType", "WALK");

        // Build spawn_data compound that contains entity
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entity);

        CompoundTag trialNBT = new CompoundTag();
        trialNBT.put("spawn_data",spawnData);
        trialNBT.putInt("target_cooldown_length", 1200);
        trialNBT.putInt("x", blockEntityPosition.getX());
        trialNBT.putInt("y", blockEntityPosition.getY());
        trialNBT.putInt("z", blockEntityPosition.getZ());

        LOGGER.info("Trial Data: '{}'", trialNBT);

        return entity;
    }
}
