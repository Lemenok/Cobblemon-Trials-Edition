package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.threads.ActiveStructureTracker;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
        boolean isSpawner = state.is(Blocks.SPAWNER);
        boolean isTrialSpawner = state.is(Blocks.TRIAL_SPAWNER);

        if (isSpawner || isTrialSpawner) {
            LOGGER.info("Spawner Found at: {}", globalBlockInfo.pos());

            ResourceLocation structureId = ActiveStructureTracker.get();

            if (structureId != null) {
                LOGGER.info("Spawner is inside structure: {}", structureId);
            } else {
                LOGGER.info("Spawner placed outside of natural generation.");
            }

            CompoundTag nbt = globalBlockInfo.nbt();
            String entityId = extractEntityId(nbt, isTrialSpawner);

            if(isSpawner)
                LOGGER.info("Entity In Spawner: {}", entityId);
            if(isTrialSpawner)
                LOGGER.info("Entity In Trial Spawner: {}", entityId);


            return new StructureTemplate.StructureBlockInfo(
                    globalBlockInfo.pos(),
                    Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState(),
                    nbt
            );
        }

        return globalBlockInfo;
    }

    private String extractEntityId(@Nullable CompoundTag nbt, boolean isTrial) {
        if (nbt == null) return "minecraft:pig";

        try {
            if (isTrial) {
                if (nbt.contains("normal_config")) {
                    CompoundTag normalConfig = nbt.getCompound("normal_config");
                    if (normalConfig.contains("spawn_potentials")) {
                        ListTag spawnPotentials = normalConfig.getList("spawn_potentials", ListTag.TAG_COMPOUND);
                        for (int i = 0; i < spawnPotentials.size(); i++) {
                            CompoundTag entry = spawnPotentials.getCompound(i);

                            CompoundTag dataTag = entry.getCompound("data");
                            CompoundTag entityTag = dataTag.getCompound("entity");

                            return entityTag.getString("id");
                        }
                    }
                }
                // Sometimes it might just be directly under spawn_data depending on the template
                if (nbt.contains("spawn_data", 10)) {
                    return nbt.getCompound("spawn_data").getCompound("entity").getString("id");
                }
            } else {
                // Standard Spawners store data in SpawnData -> entity
                if (nbt.contains("SpawnData", 10)) {
                    CompoundTag entityData = nbt.getCompound("SpawnData").getCompound("entity");
                    return entityData.getString("id");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error extracting entity. Error: ", e);
        }

        return "minecraft:pig";
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Services.PLATFORM.getSpawnerReplacementProcessor();
    }
}
