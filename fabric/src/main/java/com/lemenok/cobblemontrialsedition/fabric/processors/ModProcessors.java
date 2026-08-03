package com.lemenok.cobblemontrialsedition.fabric.processors;

import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.processor.JigsawSpawnerReplacementProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class ModProcessors {
    public static final StructureProcessorType<JigsawSpawnerReplacementProcessor> SPAWNER_REPLACEMENT_PROCESSOR = Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, "spawner_replacement_processor"),
            () -> JigsawSpawnerReplacementProcessor.CODEC
    );

    public static void register() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Processors for " + CobblemonTrialsEditionFabric.MODID);
    }
}
