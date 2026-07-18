package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.platform.Services;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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

        BlockState state = globalBlockInfo.state();
        LOGGER.info("Entered StructureProcessor");

        /*
        // Check for vanilla spawners
        if (state.is(Blocks.SPAWNER) || state.is(Blocks.TRIAL_SPAWNER)) {
            // Retrieve NBT data (entity ID) from the original block entity
            LOGGER.info("Processor for position: {}", pos);
            CompoundTag originalNbt = globalBlockInfo.nbt();
            CompoundTag newNbt = new CompoundTag();

            if (originalNbt != null)
            {}

            // Return a NEW block info with your custom spawner block
            // Use the same NBT so the data persists
            return new StructureTemplate.StructureBlockInfo(
                    globalBlockInfo.pos(),
                    Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState(),
                    newNbt
            );
        }

        // Return original if no replacement needed
        return globalBlockInfo; */

        return new StructureTemplate.StructureBlockInfo(
                globalBlockInfo.pos(),
                Blocks.GOLD_BLOCK.defaultBlockState(),
                null
        );
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Services.PLATFORM.getSpawnerReplacementProcessor();
    }
}
