package com.lemenok.cobblemontrialsedition.neoforge.events;

import com.lemenok.cobblemontrialsedition.events.SpawnerReplacementHandlerCommon;
import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.neoforge.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {

        SpawnerReplacementHandlerCommon spawnerReplacementHandlerCommon = new SpawnerReplacementHandlerCommon();

        if(!Config.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS.get())
            return;

        Level level = (Level) event.getLevel();

        // Verify that the events are chunk events.
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()) return;

        spawnerReplacementHandlerCommon.ScanChunkForEntities(serverLevel, chunk, level);
    }
}
