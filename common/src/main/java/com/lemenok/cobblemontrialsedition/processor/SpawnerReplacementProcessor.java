package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.integrations.ModConfigHelper;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.threads.ActiveStructureTracker;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpawnerReplacementProcessor extends StructureProcessor {

    public static final MapCodec<SpawnerReplacementProcessor> CODEC = MapCodec.unit(SpawnerReplacementProcessor::new);
    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(
                LevelReader level,
                BlockPos offset,
                BlockPos pos,
                StructureTemplate.StructureBlockInfo localBlockInfo,
                StructureTemplate.StructureBlockInfo globalBlockInfo,
                StructurePlaceSettings settings) {

        // Check if block is listed to be replaced from the config.
        BlockState state = globalBlockInfo.state();
        ResourceLocation BlockResourceLocation = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if(!isBlockListedToBeReplaced(BlockResourceLocation))
            return globalBlockInfo;

        // Build block processor depending on the type of block being replaced.
        IBlockProcessor blockProcessor;

        if(state.is(Blocks.SPAWNER))
            blockProcessor = new SpawnerProcessor(BlockResourceLocation);
        else if(state.is(Blocks.TRIAL_SPAWNER))
            blockProcessor = new TrialSpawnerProcessor(BlockResourceLocation);
        else
            blockProcessor = new DefaultProcessor(BlockResourceLocation);

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Spawner Found at: {}", globalBlockInfo.pos());

        // Set StructureId for processor
        blockProcessor.setStructureId(ActiveStructureTracker.get());

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS){
            if (blockProcessor.getStructureId() != null) {
                LOGGER.info("Spawner is inside structure: {}", blockProcessor.getStructureId());
            } else {
                LOGGER.info("Spawner placed outside of natural generation.");
            }
        }

        blockProcessor.setEntityid(globalBlockInfo.nbt());
        if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
            LOGGER.info("Entity to be replaced: {}", blockProcessor.getEntityId());

        // Check if structure is blacklisted
        if(ModConfigHelper.isStructureBlacklisted(level, blockProcessor))
            return globalBlockInfo;

        // Check if Spawner or Block should be replaced based on Percentages
        if(!blockProcessor.shouldBlockBeReplaced()){
            if (Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS)
                LOGGER.info("Skipped replacement of Mob Spawner at: {}", globalBlockInfo.pos());
            return globalBlockInfo;
        }

        // Check if structure has unique config to be replaced
        if(blockProcessor.doesConfigurationExistForReplacement(CacheType.STRUCTURE)) {
            return blockProcessor.buildCobblemonTrialSpawnerBlock();
            // Build Block to replace.
        }

        // Nothing was found. Return original Block.
        return globalBlockInfo;


        /*
        return new StructureTemplate.StructureBlockInfo(
                globalBlockInfo.pos(),
                Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState(),
                nbt
        );*/
    }

    private boolean isBlockListedToBeReplaced(ResourceLocation block) {
        // Add check for config.
        // ResourceLocation.fromNamespaceAndPath("farmersdelight", "stove");
        return Objects.equals(block, ResourceLocation.withDefaultNamespace("spawner")) || Objects.equals(block, ResourceLocation.withDefaultNamespace("trial_spawner"));
    }


    @Override
    protected StructureProcessorType<?> getType() {
        return Services.PLATFORM.getSpawnerReplacementProcessor();
    }
}
