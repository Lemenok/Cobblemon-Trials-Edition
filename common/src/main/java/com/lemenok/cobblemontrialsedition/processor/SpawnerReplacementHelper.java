package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.builder.IBlockBuilder;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SpawnerReplacementHelper {
    public static void processLiveSpawner(WorldGenLevel worldGenLevel, BlockPos blockPos, ResourceLocation structureId) {
        BlockState state = worldGenLevel.getBlockState(blockPos);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);

        CompoundTag nbt = blockEntity != null ? blockEntity.saveWithFullMetadata(worldGenLevel.registryAccess()) : null;
        StructureTemplate.StructureBlockInfo globalBlockInfo = new StructureTemplate.StructureBlockInfo(blockPos, state, nbt);
        IBlockBuilder blockBuilder = SpawnerReplacementProcessor.getBlockProcessor(globalBlockInfo);

        blockBuilder.setStructureId(structureId);

        if (nbt != null) blockBuilder.setEntityid(nbt);

        if (blockBuilder.shouldBlockBeReplaced() && blockBuilder.doesConfigurationExistForReplacement(CacheType.STRUCTURE)) {

            CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = blockBuilder.buildCobblemonTrialSpawnerBlock(worldGenLevel.registryAccess());
            BlockState newState = cobblemonTrialSpawnerEntity.getBlockState();

            // Remove the existing block, then set the new Spawner.
            worldGenLevel.removeBlock(blockPos, true);
            worldGenLevel.setBlock(blockPos, newState, 2);

            // Set the data of the Cobblemon Trial Spawner.
            BlockEntity newBlockEntity = worldGenLevel.getBlockEntity(blockPos);
            if (newBlockEntity != null) {
                newBlockEntity.loadWithComponents(cobblemonTrialSpawnerEntity.saveWithFullMetadata(worldGenLevel.registryAccess()), worldGenLevel.registryAccess());
                newBlockEntity.setChanged();
            }
        }
    }
}
