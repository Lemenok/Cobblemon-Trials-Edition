package com.lemenok.cobblemontrialsedition.processors;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.Config;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class ConfigProcessor {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    public static List<ResourceLocation> WHITELISTED_STRUCTURES = Collections.emptyList();

    private ConfigProcessor() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        List<String> listOfStructuresFromConfig = (List<String>) Config.WHITELISTED_STRUCTURE_LIST.get();

        if (listOfStructuresFromConfig.isEmpty()) {
            LOGGER.info("No Structures have been added to the config, setting Structure Whitelist to Empty.");
            WHITELISTED_STRUCTURES = Collections.emptyList();
            return;
        }

        List<ResourceLocation> listOfResourceLocations = new ArrayList<>(listOfStructuresFromConfig.size());
        for (String structure : listOfStructuresFromConfig) {
            if (structure == null) {
                LOGGER.warn("Found nulll entry in structures config; skipping.");
                continue;
            }
            try {
                String[] namespacePath = structure.split(":");
                ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(namespacePath[0],namespacePath[1]);
                listOfResourceLocations.add(resourceLocation);
            } catch (Exception ex) {
                LOGGER.warn("Invalid structure id in config: '{}', skipping. Error: {}", structure, ex.getMessage());
            }
        }

        WHITELISTED_STRUCTURES = Collections.unmodifiableList(listOfResourceLocations);
        LOGGER.info("Loaded {} whitelisted structures from config.", WHITELISTED_STRUCTURES.size());
    }
}
