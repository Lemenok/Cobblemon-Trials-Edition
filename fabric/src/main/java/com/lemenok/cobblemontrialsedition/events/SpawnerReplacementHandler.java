package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    public void processNewChunk(ServerLevel serverLevel, LevelChunk chunk) {

        Config modConfig = AutoConfig.getConfigHolder(Config.class).getConfig();

        if(!modConfig.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS)
            return;

        Level level = chunk.getLevel();

        if (chunk.getInhabitedTime() != 0) return;

        List<BlockEntity> listOfBlockEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfBlockEntities.add(blockEntity);
            }
        }

        if(listOfBlockEntities.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        ReplaceSpawners.Process(serverLevel, chunk, listOfBlockEntities, structureManager, level, structureRegistry);
    }
}
