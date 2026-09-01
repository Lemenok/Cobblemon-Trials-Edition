package com.lemenok.cobblemontrialsedition.config;

import com.lemenok.cobblemontrialsedition.platform.Services;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SpawnerProperties(
        List<ResourceLocation> blockTypesToReplace,
        List<ResourceLocation> mobEntitiesInSpawnerToReplace,
        int ticksBetweenSpawnAttempts,
        int spawnerCooldown,
        int playerDetectionRange,
        int spawnRange,
        int maximumNumberOfSimultaneousPokemon,
        int maximumNumberOfSimultaneousPokemonAddedPerPlayer,
        int totalNumberOfPokemonPerTrial,
        int totalNumberOfPokemonPerTrialAddedPerPlayer,
        SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables,
        SimpleWeightedRandomList<ResourceKey<LootTable>> ominousLootTables,
        boolean ominousSpawnerAttacksEnabled,
        boolean doPokemonSpawnedGlow,
        List<SpawnablePokemonProperties> listOfPokemonToSpawn,
        List<SpawnablePokemonProperties> listOfOminousPokemonToSpawn
)
{
    public static final Codec<SpawnerProperties> CODEC = RecordCodecBuilder.create(spawner -> spawner.group(
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("blockTypesToReplace", new ArrayList<>()).forGetter(SpawnerProperties::blockTypesToReplace),
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("mobEntitiesInSpawnerToReplace", new ArrayList<>()).forGetter(SpawnerProperties::mobEntitiesInSpawnerToReplace),
            Codec.INT.optionalFieldOf("ticksBetweenSpawnAttempts", 40).forGetter(SpawnerProperties::ticksBetweenSpawnAttempts),
            Codec.INT.optionalFieldOf("spawnerCooldown", 36000).forGetter(SpawnerProperties::spawnerCooldown),
            Codec.INT.optionalFieldOf("playerDetectionRange", 14).forGetter(SpawnerProperties::playerDetectionRange),
            Codec.INT.optionalFieldOf("spawnRange", 4).forGetter(SpawnerProperties::spawnRange),
            Codec.INT.optionalFieldOf("maximumNumberOfSimultaneousPokemon", 2).forGetter(SpawnerProperties::maximumNumberOfSimultaneousPokemon),
            Codec.INT.optionalFieldOf("maximumNumberOfSimultaneousPokemonAddedPerPlayer", 1).forGetter(SpawnerProperties::maximumNumberOfSimultaneousPokemonAddedPerPlayer),
            Codec.INT.optionalFieldOf("totalNumberOfPokemonPerTrial", 4).forGetter(SpawnerProperties::totalNumberOfPokemonPerTrial),
            Codec.INT.optionalFieldOf("totalNumberOfPokemonPerTrialAddedPerPlayer", 1).forGetter(SpawnerProperties::totalNumberOfPokemonPerTrialAddedPerPlayer),
            SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(Registries.LOOT_TABLE)).optionalFieldOf("lootTables", SimpleWeightedRandomList.empty()).forGetter(SpawnerProperties::lootTables),
            SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(Registries.LOOT_TABLE)).optionalFieldOf("ominousLootTables", SimpleWeightedRandomList.empty()).forGetter(SpawnerProperties::ominousLootTables),
            Codec.BOOL.optionalFieldOf("ominousSpawnerAttacksEnabled", false).forGetter(SpawnerProperties::ominousSpawnerAttacksEnabled),
            Codec.BOOL.optionalFieldOf("doPokemonSpawnedGlow", true).forGetter(SpawnerProperties::doPokemonSpawnedGlow),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfPokemonToSpawn", new ArrayList<>()).forGetter(SpawnerProperties::listOfPokemonToSpawn),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfOminousPokemonToSpawn", new ArrayList<>()).forGetter(SpawnerProperties::listOfOminousPokemonToSpawn)

    ).apply(spawner, SpawnerProperties::new));

    public SimpleWeightedRandomList<SpawnData> getListOfPokemonToSpawn(RegistryAccess registryAccess, boolean isOminous){
        SimpleWeightedRandomList.Builder<SpawnData> weightedLootTableListBuilder = new SimpleWeightedRandomList.Builder<>();

        if(isOminous){
            for(SpawnablePokemonProperties spawnablePokemonProperties: listOfOminousPokemonToSpawn){
                weightedLootTableListBuilder.add(spawnablePokemonProperties.getPokemonSpawnData(registryAccess, doPokemonSpawnedGlow), spawnablePokemonProperties.weight());
            }
        } else {
            for(SpawnablePokemonProperties spawnablePokemonProperties: listOfPokemonToSpawn){
                weightedLootTableListBuilder.add(spawnablePokemonProperties.getPokemonSpawnData(registryAccess, doPokemonSpawnedGlow), spawnablePokemonProperties.weight());
            }
        }

        return weightedLootTableListBuilder.build();
    }

    public SimpleWeightedRandomList<ResourceKey<LootTable>> getLootTables(StructureTemplate.StructureBlockInfo blockInfo, boolean isOminous, RegistryAccess registryAccess) {
        SimpleWeightedRandomList<ResourceKey<LootTable>> configuredTables = isOminous ? ominousLootTables : lootTables;

        // If config has tables mapped, return them immediately
        if (!configuredTables.unwrap().isEmpty()) {
            return configuredTables;
        }

        CompoundTag nbt = blockInfo.nbt();
        if (nbt == null) {
            return SimpleWeightedRandomList.empty();
        }

        // Catch an issue with Trial Spawners generated by Trial Chambers not having an Id for some reason.
        CompoundTag tagCopy = nbt.copy();
        if (!tagCopy.contains("id")) {
            tagCopy.putString("id", "minecraft:trial_spawner");
        }

        // Get the block entity for loot table purposes
        BlockEntity blockEntity = BlockEntity.loadStatic(blockInfo.pos(), blockInfo.state(), tagCopy, registryAccess);

        if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
            return isOminous ?
                    Objects.requireNonNull(trialSpawner.getTrialSpawner().getOminousConfig().lootTablesToEject()) :
                    Objects.requireNonNull(trialSpawner.getTrialSpawner().getNormalConfig().lootTablesToEject());
        }

        return SimpleWeightedRandomList.empty();
    }
}
