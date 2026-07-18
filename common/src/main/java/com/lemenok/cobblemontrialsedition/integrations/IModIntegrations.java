package com.lemenok.cobblemontrialsedition.integrations;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.processor.SpawnerReplacementProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public interface IModIntegrations {
    String getModID();
    Logger getLogger();
    ResourceKey<Registry<LootTable>> getLootTableRegistry();
    ResourceKey<Registry<StructureProperties>> getCobblemonTrialsStructureRegistry();
    ResourceKey<Registry<StructureProperties>> getCobblemonTrialsFeaturesRegistry();
    ResourceKey<Registry<StructureProperties>> getCobblemonTrialsDefaultStructureRegistry();
    Block getCobblemonTrialSpawnerBlock();
    BlockEntityType<CobblemonTrialSpawnerEntity> getCobblemonTrialSpawnerBlockEntity();
    SimpleParticleType getParticles();
    SoundEvent getCobblemonTrialSpawnerAmbientSound();
    SoundEvent getCobblemonTrialSpawnerAmbientOminousSound();
    StructureProcessorType<SpawnerReplacementProcessor> getSpawnerReplacementProcessor();
    ModConfigCommon getCommonConfig();
}
