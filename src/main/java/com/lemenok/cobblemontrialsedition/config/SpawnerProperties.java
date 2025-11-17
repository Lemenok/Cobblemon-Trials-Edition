package com.lemenok.cobblemontrialsedition.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SpawnerProperties(
        List<ResourceLocation> blockEntityTypesToReplace,
        List<ResourceLocation> mobEntitiesInSpawnerToReplace,
        int ticksBetweenSpawnAttempts,
        int spawnerCooldown,
        int playerDetectionRange,
        int spawnRange,
        int maximumNumberOfSimultaneousPokemon,
        int maximumNumberOfSimultaneousPokemonAddedPerPlayer,
        int totalNumberOfPokemonPerTrial,
        int totalNumberOfPokemonPerTrialAddedPerPlayer,
        Optional<List<ResourceLocation>> lootTables,
        Optional<List<ResourceLocation>> ominousLootTables,
        boolean ominousSpawnerAttacksEnabled,
        boolean doPokemonSpawnedGlow,
        List<SpawnablePokemonProperties> listOfPokemonToSpawn,
        List<SpawnablePokemonProperties> listOfOminousPokemonToSpawn
)
{
    public static final Codec<SpawnerProperties> CODEC = RecordCodecBuilder.create(spawner -> spawner.group(
            Codec.list(ResourceLocation.CODEC).fieldOf("blockEntityTypesToReplace").forGetter(SpawnerProperties::blockEntityTypesToReplace),
            Codec.list(ResourceLocation.CODEC).fieldOf("mobEntitiesInSpawnerToReplace").forGetter(SpawnerProperties::mobEntitiesInSpawnerToReplace),
            Codec.INT.optionalFieldOf("ticksBetweenSpawnAttempts", 40).forGetter(SpawnerProperties::ticksBetweenSpawnAttempts),
            Codec.INT.optionalFieldOf("spawnerCooldown", 40).forGetter(SpawnerProperties::spawnerCooldown),
            Codec.INT.optionalFieldOf("playerDetectionRange", 40).forGetter(SpawnerProperties::playerDetectionRange),
            Codec.INT.optionalFieldOf("spawnRange", 40).forGetter(SpawnerProperties::spawnRange),
            Codec.INT.optionalFieldOf("maximumNumberOfSimultaneousPokemon", 40).forGetter(SpawnerProperties::maximumNumberOfSimultaneousPokemon),
            Codec.INT.optionalFieldOf("maximumNumberOfSimultaneousPokemonAddedPerPlayer", 40).forGetter(SpawnerProperties::maximumNumberOfSimultaneousPokemonAddedPerPlayer),
            Codec.INT.optionalFieldOf("totalNumberOfPokemonPerTrial", 40).forGetter(SpawnerProperties::totalNumberOfPokemonPerTrial),
            Codec.INT.optionalFieldOf("totalNumberOfPokemonPerTrialAddedPerPlayer", 40).forGetter(SpawnerProperties::totalNumberOfPokemonPerTrialAddedPerPlayer),
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("lootTables").forGetter(SpawnerProperties::lootTables),
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("ominousLootTables").forGetter(SpawnerProperties::ominousLootTables),
            Codec.BOOL.optionalFieldOf("ominousSpawnerAttacksEnabled", false).forGetter(SpawnerProperties::ominousSpawnerAttacksEnabled),
            Codec.BOOL.optionalFieldOf("doPokemonSpawnedGlow", true).forGetter(SpawnerProperties::doPokemonSpawnedGlow),
            Codec.list(SpawnablePokemonProperties.CODEC).fieldOf("listOfPokemonToSpawn").forGetter(SpawnerProperties::listOfPokemonToSpawn),
            Codec.list(SpawnablePokemonProperties.CODEC).fieldOf("listOfOminousPokemonToSpawn").forGetter(SpawnerProperties::listOfOminousPokemonToSpawn)

    ).apply(spawner, SpawnerProperties::new));

    public boolean doesSpawnerSettingsContainEntityType(Level level, EntityType entity, BlockEntity blockEntity) {
        Registry<EntityType<?>> entityRegistry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        Registry<BlockEntityType<?>> blockEntityTypeRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK_ENTITY_TYPE);

        // Iterate through blockEntityTypes and Entitytypes list to make sure that it contains the correct match.
        // If it does return true.
        for (ResourceLocation blockEntityResourceLocation: blockEntityTypesToReplace){
            if(blockEntityResourceLocation.equals(blockEntityTypeRegistry.getKey(blockEntity.getType()))){
                for (ResourceLocation entityTypeResourceLocation: mobEntitiesInSpawnerToReplace){
                    if(entityTypeResourceLocation.equals(entityRegistry.getKey(entity))){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public SimpleWeightedRandomList<SpawnData> getListOfPokemonToSpawn(ServerLevel level, boolean isOminous){
        SimpleWeightedRandomList.Builder<SpawnData> weightedLootTableListBuilder = new SimpleWeightedRandomList.Builder<>();

        if(isOminous){
            for(SpawnablePokemonProperties spawnablePokemonProperties: listOfOminousPokemonToSpawn){
                weightedLootTableListBuilder.add(spawnablePokemonProperties.getPokemonSpawnData(level, doPokemonSpawnedGlow), spawnablePokemonProperties.weight());
            }
        } else {
            for(SpawnablePokemonProperties spawnablePokemonProperties: listOfPokemonToSpawn){
                weightedLootTableListBuilder.add(spawnablePokemonProperties.getPokemonSpawnData(level, doPokemonSpawnedGlow), spawnablePokemonProperties.weight());
            }
        }

        return weightedLootTableListBuilder.build();
    }

    public SimpleWeightedRandomList<ResourceKey<LootTable>> getLootTables(BlockEntity blockEntity, boolean isOminous){

        // If the block entity is a trial spawner and no loot tables are listed, we default to the original Loot table drops.
        if(blockEntity instanceof TrialSpawnerBlockEntity && lootTables.isEmpty())
            return Objects.requireNonNull(((TrialSpawnerBlockEntity) blockEntity).getTrialSpawner().getNormalConfig().lootTablesToEject());

        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> weightedLootTableListBuilder = new SimpleWeightedRandomList.Builder<>();

        if(isOminous){
            addLootTableToWeightedList(lootTables, weightedLootTableListBuilder);
        }
        else {
            addLootTableToWeightedList(ominousLootTables, weightedLootTableListBuilder);
        }

        return weightedLootTableListBuilder.build();
    }

    private void addLootTableToWeightedList(Optional<List<ResourceLocation>> lootTables, SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> weightedLootTableListBuilder) {

        for (ResourceLocation resourceLocation : lootTables.get()) {
            ResourceKey<LootTable> lootTableResourceKey = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation);
            weightedLootTableListBuilder.add(lootTableResourceKey);
        }
    }
}
