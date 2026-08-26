package com.lemenok.cobblemontrialsedition.neoforge.integrations;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.integrations.IModIntegrations;
import com.lemenok.cobblemontrialsedition.integrations.ModConfigCommon;
import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.neoforge.Config;
import com.lemenok.cobblemontrialsedition.neoforge.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.neoforge.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.neoforge.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.neoforge.processor.ModProcessors;
import com.lemenok.cobblemontrialsedition.neoforge.sound.ModSounds;
import com.lemenok.cobblemontrialsedition.processor.JigsawSpawnerReplacementProcessor;
import com.lemenok.cobblemontrialsedition.network.OpenSpawnerConfigS2CPacket;
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
        return CobblemonTrialsEdition.MODID;
    }

    @Override
    public Logger getLogger() {
        return CobblemonTrialsEdition.LOGGER;
    }

    @Override
    public ResourceKey<Registry<LootTable>> getLootTableRegistry() {
        return CobblemonTrialsEdition.RegistryEvents.COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY;
    }

    @Override
    public ResourceKey<Registry<StructureProperties>> getCobblemonTrialsStructureRegistry() {
        return CobblemonTrialsEdition.RegistryEvents.COBBLEMON_TRIALS_STRUCTURE_REGISTRY;
    }

    @Override
    public ResourceKey<Registry<StructureProperties>> getCobblemonTrialsDefaultStructureRegistry() {
        return CobblemonTrialsEdition.RegistryEvents.COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY;
    }

    @Override
    public Block getCobblemonTrialSpawnerBlock() {
        return ModBlocks.COBBLEMON_TRIAL_SPAWNER.get();
    }

    @Override
    public BlockEntityType<CobblemonTrialSpawnerEntity> getCobblemonTrialSpawnerBlockEntity() {
        return ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get();
    }

    @Override
    public SimpleParticleType getParticles() {
        return ModParticles.UNOWN_PARTICLES.get();
    }

    @Override
    public SoundEvent getCobblemonTrialSpawnerAmbientSound() {
        return ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT.get();
    }

    @Override
    public SoundEvent getCobblemonTrialSpawnerAmbientOminousSound() {
        return ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS.get();
    }

    @Override
    public StructureProcessorType<JigsawSpawnerReplacementProcessor> getSpawnerReplacementProcessor() {
        return ModProcessors.SPAWNER_REPLACEMENT_PROCESSOR.get();
    }

    @Override
    public void sendSpawnerConfigPacket(ServerPlayer player, BlockPos pos, SpawnerProperties properties) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player,
                new OpenSpawnerConfigS2CPacket(pos, properties)
        );
    }

    @Override
    public ModConfigCommon getCommonConfig() {
        ModConfigCommon modConfigCommon = new ModConfigCommon();
        modConfigCommon.ENABLE_DEBUG_LOGS = Config.ENABLE_DEBUG_LOGS.get();
        modConfigCommon.ENABLE_TRIAL_POTION_RECIPE = Config.ENABLE_TRIAL_POTION_RECIPE.get();
        modConfigCommon.BLACKLISTED_STRUCTURE_IDS = Config.BLACKLISTED_STRUCTURE_IDS.get();
        modConfigCommon.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = Config.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS.get();
        modConfigCommon.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = Config.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS.get();
        modConfigCommon.REPLACE_ANY_BLOCKS_WITH_COBBLEMON_SPAWNERS = Config.REPLACE_ANY_BLOCKS_WITH_COBBLEMON_SPAWNERS.get();
        modConfigCommon.REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = Config.REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS.get();
        modConfigCommon.SPAWNED_POKEMON_ARE_UNCATCHABLE = Config.SPAWNED_POKEMON_ARE_UNCATCHABLE.get();
        modConfigCommon.SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE = Config.SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE.get();
        modConfigCommon.ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE = Config.ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE.get();
        modConfigCommon.MOB_SPAWNER_REPLACEMENT_PERCENTAGE = Config.MOB_SPAWNER_REPLACEMENT_PERCENTAGE.get();
        modConfigCommon.TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE = Config.TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE.get();
        modConfigCommon.BLOCK_REPLACEMENT_PERCENTAGE = Config.BLOCK_REPLACEMENT_PERCENTAGE.get();
        modConfigCommon.ALPHA_POKEMON_PERCENTAGE = Config.ALPHA_POKEMON_PERCENTAGE.get();
        modConfigCommon.ENABLE_POKEMON_LEVEL_ADJUSTMENT = Config.ENABLE_POKEMON_LEVEL_ADJUSTMENT.get();
        modConfigCommon.POKEMON_LEVEL_ADJUSTMENT_TYPE = Config.POKEMON_LEVEL_ADJUSTMENT_TYPE.get();
        ModConfigCommon.TIME_TILL_POKEMON_DESPAWN_IN_TICKS = Config.TIME_TILL_POKEMON_DESPAWN_IN_TICKS.get();

        return modConfigCommon;
    }
}
