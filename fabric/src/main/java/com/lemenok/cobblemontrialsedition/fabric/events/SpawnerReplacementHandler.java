package com.lemenok.cobblemontrialsedition.fabric.events;

import com.lemenok.cobblemontrialsedition.events.SpawnerReplacementHandlerCommon;
import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.fabric.Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpawnerReplacementHandler {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    public void processNewChunk(ServerLevel serverLevel, LevelChunk chunk) {

        Config modConfig = AutoConfig.getConfigHolder(Config.class).getConfig();
        SpawnerReplacementHandlerCommon spawnerReplacementHandlerCommon = new SpawnerReplacementHandlerCommon();

        if(!modConfig.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS)
            return;

        Level level = chunk.getLevel();

        if (chunk.getInhabitedTime() != 0) return;

        spawnerReplacementHandlerCommon.ScanChunkForEntities(serverLevel, chunk, level);
    }
}
