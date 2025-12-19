package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {

        if(!Config.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS.get())
            return;

        Level level = (Level) event.getLevel();

        // Verify that the events are chunk events.
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()) return;
        
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
