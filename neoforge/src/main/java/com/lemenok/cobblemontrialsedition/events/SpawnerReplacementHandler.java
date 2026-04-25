package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

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
        
        List<BlockEntity> listOfSpawnerEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfSpawnerEntities.add(blockEntity);
            }
        }

        List<BlockEntity> listOfShriekers = new ArrayList<>();

        if(Config.REPLACE_ANY_SKULK_SHRIEKERS_WITH_COBBLEMON_SPAWNERS.get())
            listOfShriekers = scanChunkForShriekers(chunk);

        List<BlockEntity> listOfBlockEntities =
                Stream.concat(listOfSpawnerEntities.stream(),listOfShriekers.stream()).toList();

        if(listOfBlockEntities.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        ReplaceSpawners.Process(serverLevel, chunk, listOfBlockEntities, structureManager, level, structureRegistry);
    }

    private List<BlockEntity> scanChunkForShriekers(LevelChunk chunk) {
        List<BlockEntity> listOfShriekers = new ArrayList<>();

        int minBuildHeight = chunk.getLevel().getMinBuildHeight();

        // Iterate through each 16x16x16 section in the chunk column
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);

            if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(Blocks.SCULK_SHRIEKER))) {
                continue;
            }

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);

                        if (state.is(Blocks.SCULK_SHRIEKER)) {
                            int worldX = chunk.getPos().getMinBlockX() + x;
                            int worldY = sectionIndex * 16 + minBuildHeight + y;
                            int worldZ = chunk.getPos().getMinBlockZ() + z;

                            BlockPos foundPos = new BlockPos(worldX, worldY, worldZ);

                            listOfShriekers.add(new SculkShriekerBlockEntity(foundPos, state));

                            if(Config.ENABLE_DEBUG_LOGS.get())
                                LOGGER.info("Found shrieker at: {}", foundPos);
                        }
                    }
                }
            }
        }

        return listOfShriekers;
    }
}
