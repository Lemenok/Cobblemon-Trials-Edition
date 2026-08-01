package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.builder.DefaultBuilder;
import com.lemenok.cobblemontrialsedition.builder.IBlockBuilder;
import com.lemenok.cobblemontrialsedition.builder.SpawnerBuilder;
import com.lemenok.cobblemontrialsedition.builder.TrialSpawnerBuilder;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.integrations.ModConfigHelper;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.threads.ActiveStructureTracker;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// The class that handles the StructureProcessor (Jigsaw placed structures)
public class JigsawSpawnerReplacementProcessor extends StructureProcessor {

    public static final MapCodec<JigsawSpawnerReplacementProcessor> CODEC = MapCodec.unit(JigsawSpawnerReplacementProcessor::new);
    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos,
                StructureTemplate.StructureBlockInfo localBlockInfo, StructureTemplate.StructureBlockInfo globalBlockInfo,
                StructurePlaceSettings settings) {

        // Is Spawner replacement active.
        if(!Services.PLATFORM.getCommonConfig().REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS)
            return globalBlockInfo;

        ResourceLocation currentBlockId = BuiltInRegistries.BLOCK.getKey(globalBlockInfo.state().getBlock());
        ResourceLocation structureId = ActiveStructureTracker.get();

        // Check if it's in the custom Replacement Blocks dictionary
        ResourceLocation mappedEntityId = null;
        if (structureId != null) {
            // Retrieve the StructureProperties for the active structure.
            StructureProperties structureProperties = PropertiesCache.getStructureProperties(structureId);

            if (structureProperties != null && structureProperties.uniqueReplacementBlocks() != null && structureProperties.uniqueReplacementBlocks().containsKey(currentBlockId)) {
                mappedEntityId = structureProperties.uniqueReplacementBlocks().get(currentBlockId);
            }
        }

        // If it's neither a standard spawner nor a mapped custom block, skip it
        if (!isBlockSpawner(currentBlockId) && mappedEntityId == null) {
            return globalBlockInfo;
        }

        // Build block processor depending on the type of block being replaced.
        IBlockBuilder blockBuilder = getBlockProcessor(globalBlockInfo, mappedEntityId);

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Block Replacement Found at: {}", globalBlockInfo.pos());

        // Set StructureId for processor
        blockBuilder.setStructureId(structureId);

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS){
            if (blockBuilder.getStructureId() != null) {
                LOGGER.info("Spawner is inside structure: {}", blockBuilder.getStructureId());
            } else {
                LOGGER.info("Spawner placed outside of natural generation.");
            }
        }

        blockBuilder.setEntityid(globalBlockInfo.nbt());
        if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Entity to be replaced: {}", blockBuilder.getEntityId());

        // Check if structure is blacklisted
        if(ModConfigHelper.isStructureBlacklisted(level, blockBuilder))
            return globalBlockInfo;

        // Check if Spawner or Block should be replaced based on Percentages
        if(!blockBuilder.shouldBlockBeReplaced()){
            if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                LOGGER.info("Skipped replacement of Spawner at: {} due to Percentage based replacement.", globalBlockInfo.pos());
            return globalBlockInfo;
        }

        // Check if structure has unique config to be replaced
        if(blockBuilder.doesConfigurationExistForReplacement(CacheType.STRUCTURE)) {

            ServerLevel serverLevel = null;
            if (level instanceof ServerLevel sl) {
                serverLevel = sl;
            } else if (level instanceof WorldGenLevel wgl) {
                serverLevel = wgl.getLevel();
            }

            BlockState newBlockState = Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState();
            CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = blockBuilder.buildCobblemonTrialSpawnerBlock(level.registryAccess(), serverLevel);

            // Serialize the fully configured BlockEntity into an NBT tag
            CompoundTag newNbt = cobblemonTrialSpawnerEntity.saveWithFullMetadata(level.registryAccess());

            return new StructureTemplate.StructureBlockInfo(blockBuilder.getBlockPosition(), newBlockState, newNbt);
        }

        // Nothing was found. Return original Block.
        return globalBlockInfo;
    }

    public static @NotNull IBlockBuilder getBlockProcessor(StructureTemplate.StructureBlockInfo blockInfo, ResourceLocation mappedEntityId) {
        IBlockBuilder blockProcessor;

        if(blockInfo.state().is(Blocks.SPAWNER))
            blockProcessor = new SpawnerBuilder(blockInfo);
        else if(blockInfo.state().is(Blocks.TRIAL_SPAWNER))
            blockProcessor = new TrialSpawnerBuilder(blockInfo);
        else
            blockProcessor = new DefaultBuilder(blockInfo, mappedEntityId);
        return blockProcessor;
    }

    private boolean isBlockSpawner(ResourceLocation block) {
        return Objects.equals(block, ResourceLocation.withDefaultNamespace("spawner")) || Objects.equals(block, ResourceLocation.withDefaultNamespace("trial_spawner"));
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Services.PLATFORM.getSpawnerReplacementProcessor();
    }
}
