package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.builder.IBlockBuilder;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.integrations.ModConfigHelper;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The class that handles all other types of Structures not included in Jigsaws. (Mineshaft, Strongholds, Fortresses, Sculk Patches etc.)
public class StructureSpawnerReplacementProcessor {

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    public static void processSpawner(WorldGenLevel worldGenLevel, BlockPos blockPos, ResourceLocation structureId) {

        // Is Spawner replacement active.
        if(!Services.PLATFORM.getCommonConfig().REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS)
            return;

        BlockState state = worldGenLevel.getBlockState(blockPos);
        ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        ResourceLocation mappedEntityId = null;
        if (structureId != null) {
            StructureProperties structureProperties = PropertiesCache.getStructureProperties(structureId);
            if (structureProperties != null && structureProperties.uniqueReplacementBlocks() != null && structureProperties.uniqueReplacementBlocks().containsKey(currentBlockId)) {
                mappedEntityId = structureProperties.uniqueReplacementBlocks().get(currentBlockId);
            }
        }

        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);
        CompoundTag nbt = blockEntity != null ? blockEntity.saveWithFullMetadata(worldGenLevel.registryAccess()) : null;
        StructureTemplate.StructureBlockInfo globalBlockInfo = new StructureTemplate.StructureBlockInfo(blockPos, state, nbt);

        IBlockBuilder blockBuilder = JigsawSpawnerReplacementProcessor.getBlockProcessor(globalBlockInfo, mappedEntityId);

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Block/Spawner Found at: {}", blockPos);

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Block/Spawner is inside structure: {}", structureId);

        blockBuilder.setStructureId(structureId);

        // Check if structure is blacklisted
        if(ModConfigHelper.isStructureBlacklisted(worldGenLevel.getLevel(), blockBuilder))
            return;

        if (nbt != null) blockBuilder.setEntityid(nbt);

        if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Entity to be replaced: {}", blockBuilder.getEntityId());

        // Check if Spawner or Block should be replaced based on Percentages
        if(!blockBuilder.shouldBlockBeReplaced()){
            if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                LOGGER.info("Skipped replacement of Spawner at: {} due to Percentage based replacement.", globalBlockInfo.pos());
            return;
        }

        if (blockBuilder.doesConfigurationExistForReplacement(CacheType.STRUCTURE)) {

            ServerLevel serverLevel = null;
            if (worldGenLevel instanceof ServerLevel sl) {
                serverLevel = sl;
            } else if (worldGenLevel instanceof WorldGenLevel wgl) {
                serverLevel = wgl.getLevel();
            }

            CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = blockBuilder.buildCobblemonTrialSpawnerBlock(worldGenLevel.registryAccess(), serverLevel);
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
