package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.processors.ConfigProcessor;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkGenerationHandler {
    // TODO: Create Centralized Logger
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    // Keep track of chunks we have already processed.
    //private static final Set<Long> processedChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Level level = (Level) event.getLevel();

        if(level.isClientSide()) return; //We only care about server side level events
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()){
            LOGGER.debug("Chunk '{}' is not new", chunk.getPos());
            return;
        }

        // Collect positions of spawners first
        List<BlockPos> spawnersToReplace = new ArrayList<>();
        for (BlockEntity anyBlockEntity : chunk.getBlockEntities().values()) {
            if (anyBlockEntity instanceof SpawnerBlockEntity) {
                spawnersToReplace.add(anyBlockEntity.getBlockPos());
            }
        }

        if (spawnersToReplace.isEmpty()) return;

        for (BlockPos blockEntityPosition : spawnersToReplace) {
            try {
                if (!serverLevel.isLoaded(blockEntityPosition)) continue;
                BlockEntity blockEntity = serverLevel.getBlockEntity(blockEntityPosition);
                if (!(blockEntity instanceof SpawnerBlockEntity)) continue;

                StructureManager structureManager = serverLevel.structureManager();
                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

                // Check to see if the Structure is on the CustomSpawner List
                if (isStructurePresentAt(allStructuresAtPosition)) {
                    serverLevel.setBlock(blockEntityPosition, Blocks.TRIAL_SPAWNER.defaultBlockState(), 3);
                    LOGGER.info("Replaced Structure Spawner at Location '{}' '{}' '{}'", blockEntityPosition.getX(), blockEntityPosition.getY(), blockEntityPosition.getZ());
                    return;
                }
                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                else if (allStructuresAtPosition.isEmpty()) {
                    serverLevel.setBlock(blockEntityPosition, Blocks.TRIAL_SPAWNER.defaultBlockState(), 3);
                    LOGGER.info("Replaced Spawner at Location '{}' '{}' '{}'", blockEntityPosition.getX(), blockEntityPosition.getY(), blockEntityPosition.getZ());
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

    public static boolean isChunkFullyGenerated(ChunkAccess chunk) {
        return chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL);
    }
}
