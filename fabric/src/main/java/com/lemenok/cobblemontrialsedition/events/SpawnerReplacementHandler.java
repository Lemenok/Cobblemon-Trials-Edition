package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import me.shedaniel.autoconfig.AutoConfig;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    public void processNewChunk(ServerLevel serverLevel, LevelChunk chunk) {

        Config modConfig = AutoConfig.getConfigHolder(Config.class).getConfig();

        if(!modConfig.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS)
            return;

        Level level = chunk.getLevel();

        if (chunk.getInhabitedTime() != 0) return;

        List<BlockEntity> listOfSpawnerEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfSpawnerEntities.add(blockEntity);
            }
        }

        List<BlockEntity> listOfShriekers = new ArrayList<>();

        if(modConfig.REPLACE_ANY_SKULK_SHRIEKERS_WITH_COBBLEMON_SPAWNERS)
            listOfShriekers = scanChunkForShriekers(chunk, modConfig);

        List<BlockEntity> listOfBlockEntities =
                Stream.concat(listOfSpawnerEntities.stream(),listOfShriekers.stream()).toList();

        if(listOfBlockEntities.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        ReplaceSpawners.Process(serverLevel, chunk, listOfBlockEntities, structureManager, level, structureRegistry);
    }

    private List<BlockEntity> scanChunkForShriekers(LevelChunk chunk, Config modConfig) {
        List<BlockEntity> listOfShriekers = new ArrayList<>();

        int minBuildHeight = chunk.getLevel().getMinBuildHeight();

        // 1. Iterate through each 16x16x16 section in the chunk column
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);

            // Optimization: Skip if the section is empty or definitely doesn't have shriekers
            if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(Blocks.SCULK_SHRIEKER))) {
                continue;
            }

            // 2. Iterate the 3D grid of the section
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);

                        if (state.is(Blocks.SCULK_SHRIEKER)) {
                            // 3. Convert local section coordinates to world coordinates
                            int worldX = chunk.getPos().getMinBlockX() + x;
                            int worldY = sectionIndex * 16 + minBuildHeight + y;
                            int worldZ = chunk.getPos().getMinBlockZ() + z;

                            BlockPos foundPos = new BlockPos(worldX, worldY, worldZ);

                            listOfShriekers.add(new SculkShriekerBlockEntity(foundPos, state));

                            if(modConfig.ENABLE_DEBUG_LOGS)
                                LOGGER.info("Found shrieker at: {}", foundPos);
                        }
                    }
                }
            }
        }
        
        return listOfShriekers;
    }
}
