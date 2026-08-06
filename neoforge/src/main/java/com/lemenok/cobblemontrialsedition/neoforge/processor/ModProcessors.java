package com.lemenok.cobblemontrialsedition.neoforge.processor;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.processor.JigsawSpawnerReplacementProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS =
        DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, CobblemonTrialsEdition.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<JigsawSpawnerReplacementProcessor>> SPAWNER_REPLACEMENT_PROCESSOR =
        STRUCTURE_PROCESSORS.register("spawner_replacement_processor", () -> () -> JigsawSpawnerReplacementProcessor.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSORS.register(eventBus);
    }
}
