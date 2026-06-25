package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
