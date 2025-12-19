package com.lemenok.cobblemontrialsedition;

import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonTrialsEditionFabric implements ModInitializer {
    public static final String MODID = "cobblemontrialsedition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
    }
}
