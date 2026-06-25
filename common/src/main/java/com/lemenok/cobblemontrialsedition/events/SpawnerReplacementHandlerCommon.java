package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SpawnerReplacementHandlerCommon {
    public void ScanChunkForEntities(ServerLevel serverLevel, LevelChunk chunk, Level level) {
        List<BlockEntity> listOfSpawnerEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfSpawnerEntities.add(blockEntity);
            }
        }

        List<BlockEntity> listOfShriekers = new ArrayList<>();

        if(Services.PLATFORM.getCommonConfig().REPLACE_ANY_SKULK_SHRIEKERS_WITH_COBBLEMON_SPAWNERS)
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

                            if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                                Services.PLATFORM.getLogger().info("Found shrieker at: {}", foundPos);
                        }
                    }
                }
            }
        }

        return listOfShriekers;
    }
}
