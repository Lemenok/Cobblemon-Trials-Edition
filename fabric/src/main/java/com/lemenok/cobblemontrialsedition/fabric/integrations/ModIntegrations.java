package com.lemenok.cobblemontrialsedition.fabric.integrations;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.fabric.Config;
import com.lemenok.cobblemontrialsedition.fabric.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.fabric.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.fabric.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.fabric.sound.ModSounds;
import com.lemenok.cobblemontrialsedition.integrations.IModIntegrations;
import com.lemenok.cobblemontrialsedition.integrations.ModConfigCommon;
import com.lemenok.cobblemontrialsedition.processor.JigsawSpawnerReplacementProcessor;
import com.lemenok.cobblemontrialsedition.network.OpenSpawnerConfigS2CPacket;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class ModIntegrations implements IModIntegrations {
    @Override
    public String getModID() {
        return CobblemonTrialsEditionFabric.MODID;
    }

    @Override
    public Logger getLogger() {
        return CobblemonTrialsEditionFabric.LOGGER;
    }

    @Override
    public ResourceKey<Registry<LootTable>> getLootTableRegistry() {
        return CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY;
    }

    @Override
    public ResourceKey<Registry<StructureProperties>> getCobblemonTrialsStructureRegistry() {
        return CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_STRUCTURE_REGISTRY;
    }

    @Override
    public ResourceKey<Registry<StructureProperties>> getCobblemonTrialsDefaultStructureRegistry() {
        return CobblemonTrialsEditionFabric.COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY;
    }

    @Override
    public Block getCobblemonTrialSpawnerBlock() {
        return ModBlocks.COBBLEMON_TRIAL_SPAWNER;
    }

    @Override
    public BlockEntityType<CobblemonTrialSpawnerEntity> getCobblemonTrialSpawnerBlockEntity() {
        return ModBlockEntities.COBBLEMON_TRIAL_SPAWNER;
    }

    @Override
    public SimpleParticleType getParticles() {
        return ModParticles.UNOWN_PARTICLES;
    }

    @Override
    public SoundEvent getCobblemonTrialSpawnerAmbientSound() {
        return ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT;
    }

    @Override
    public SoundEvent getCobblemonTrialSpawnerAmbientOminousSound() {
        return ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS;
    }

    @Override
    public StructureProcessorType<JigsawSpawnerReplacementProcessor> getSpawnerReplacementProcessor() {
        return null;
    }

    @Override
    public void sendSpawnerConfigPacket(ServerPlayer player, BlockPos pos, SpawnerProperties properties) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                player,
                new OpenSpawnerConfigS2CPacket(pos, properties)
        );
    }

    @Override
    public ModConfigCommon getCommonConfig() {
        Config modConfig = AutoConfig.getConfigHolder(Config.class).getConfig();

        ModConfigCommon modConfigCommon = new ModConfigCommon();
        modConfigCommon.ENABLE_DEBUG_LOGS = modConfig.ENABLE_DEBUG_LOGS;
        modConfigCommon.ENABLE_TRIAL_POTION_RECIPE = modConfig.ENABLE_TRIAL_POTION_RECIPE;
        modConfigCommon.BLACKLISTED_STRUCTURE_IDS = modConfig.BLACKLISTED_STRUCTURE_IDS;
        modConfigCommon.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = modConfig.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS;
        modConfigCommon.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = modConfig.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS;
        modConfigCommon.REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = modConfig.REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS;
        modConfigCommon.REPLACE_ANY_BLOCKS_WITH_COBBLEMON_SPAWNERS = modConfig.REPLACE_ANY_BLOCKS_WITH_COBBLEMON_SPAWNERS;
        modConfigCommon.SPAWNED_POKEMON_ARE_UNCATCHABLE = modConfig.SPAWNED_POKEMON_ARE_UNCATCHABLE;
        modConfigCommon.SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE = modConfig.SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE;
        modConfigCommon.ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE = modConfig.ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE;
        modConfigCommon.MOB_SPAWNER_REPLACEMENT_PERCENTAGE = modConfig.MOB_SPAWNER_REPLACEMENT_PERCENTAGE;
        modConfigCommon.TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE = modConfig.TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE;
        modConfigCommon.BLOCK_REPLACEMENT_PERCENTAGE = modConfig.BLOCK_REPLACEMENT_PERCENTAGE;
        modConfigCommon.ALPHA_POKEMON_PERCENTAGE = modConfig.ALPHA_POKEMON_PERCENTAGE;
        modConfigCommon.ENABLE_POKEMON_LEVEL_ADJUSTMENT = modConfig.ENABLE_POKEMON_LEVEL_ADJUSTMENT;
        modConfigCommon.POKEMON_LEVEL_ADJUSTMENT_TYPE = modConfig.POKEMON_LEVEL_ADJUSTMENT_TYPE;
        ModConfigCommon.TIME_TILL_POKEMON_DESPAWN_IN_TICKS = modConfig.TIME_TILL_POKEMON_DESPAWN_IN_TICKS;;

        return modConfigCommon;
    }
}
