package com.lemenok.cobblemontrialsedition.integrations;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.processor.JigsawSpawnerReplacementProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

import java.util.List;

public interface IModIntegrations {
    String getModID();
    Logger getLogger();
    ResourceKey<Registry<LootTable>> getLootTableRegistry();
    ResourceKey<Registry<StructureProperties>> getCobblemonTrialsStructureRegistry();
    ResourceKey<Registry<StructureProperties>> getCobblemonTrialsDefaultStructureRegistry();
    Block getCobblemonTrialSpawnerBlock();
    BlockEntityType<CobblemonTrialSpawnerEntity> getCobblemonTrialSpawnerBlockEntity();
    SimpleParticleType getParticles();
    SoundEvent getCobblemonTrialSpawnerAmbientSound();
    SoundEvent getCobblemonTrialSpawnerAmbientOminousSound();
    StructureProcessorType<JigsawSpawnerReplacementProcessor> getSpawnerReplacementProcessor();
    void sendSpawnerConfigPacket(ServerPlayer player, BlockPos pos, SpawnerProperties properties, List<ResourceLocation> availableLootTables);
    ModConfigCommon getCommonConfig();
}
