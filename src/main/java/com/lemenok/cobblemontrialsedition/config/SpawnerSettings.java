package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public class SpawnerSettings {
    private final List<BlockEntityType> SpawnerTypesToReplace;
    private final List<EntityType> SpawnerEntityToReplace;
    private final int TicksBetweenSpawnAttempts;
    private final int SpawnerCooldown;
    private final int PlayerDetectionRange;
    private final int SpawnRange;
    private final int MaximumNumberOfSimultaneousPokemon;
    private final int MaximumNumberOfSimultaneousPokemonAddedPerPlayer;
    private final int TotalNumberOfPokemonPerTrial;
    private final int TotalNumberOfPokemonPerTrialAddedPerPlayer;
    private LootTable SpawnerLootTable;
    private LootTable SpawnerOminousLootTable;
    private final boolean DisableOminousSpawnerAttacks;
    private SimpleWeightedRandomList<PokemonProperties> ListOfPokemonToSpawn;
    private SimpleWeightedRandomList<PokemonProperties> ListOfOminousPokemonToSpawn;

    private final ResourceLocation SpawnerType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(BlockEntityType.MOB_SPAWNER);
    private final ResourceLocation TrialSpawnerType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(BlockEntityType.TRIAL_SPAWNER);

    public SpawnerSettings(int ticksBetweenSpawnAttempts, int spawnerCooldown, int playerDetectionRange, int spawnRange,
                           int maximumNumberOfSimultaneousPokemon, int maximumNumberOfSimultaneousPokemonAddedPerPlayer,
                           int totalNumberOfPokemonPerTrial, int totalNumberOfPokemonPerTrialAddedPerPlayer, boolean disableOminousSpawnerAttacks){

        TicksBetweenSpawnAttempts = ticksBetweenSpawnAttempts;
        SpawnerCooldown = spawnerCooldown;
        PlayerDetectionRange = playerDetectionRange;
        SpawnRange = spawnRange;
        MaximumNumberOfSimultaneousPokemon = maximumNumberOfSimultaneousPokemon;
        MaximumNumberOfSimultaneousPokemonAddedPerPlayer = maximumNumberOfSimultaneousPokemonAddedPerPlayer;
        TotalNumberOfPokemonPerTrial = totalNumberOfPokemonPerTrial;
        TotalNumberOfPokemonPerTrialAddedPerPlayer = totalNumberOfPokemonPerTrialAddedPerPlayer;
        DisableOminousSpawnerAttacks = disableOminousSpawnerAttacks;

        SpawnerTypesToReplace = new ArrayList<>();
        SpawnerEntityToReplace = new ArrayList<>();
    }

    public void SetSpawnerTypesToReplace(String[] spawnerTypesToReplace){

        for(String spawnerTypeToReplace: spawnerTypesToReplace){
            String[] splitString = spawnerTypeToReplace.split(":");
            ResourceLocation spawnerTypeResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);

            // At the moment we are only allowing Spawners and Trial Spawners to be replaced.
            // Other logic in here is future proofing for other types I may decide on adding.
            if(spawnerTypeResourceLocation.equals(SpawnerType) || spawnerTypeResourceLocation.equals(TrialSpawnerType) )
            {
                BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(spawnerTypeResourceLocation)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown Block Entity: " + spawnerTypeResourceLocation));

                SpawnerTypesToReplace.add(blockEntityType);
            }
        }
    }

    public void SetSpawnerEntityToReplace(String[] entitiesToReplace){

        for(String entity: entitiesToReplace){
            String[] splitString = entity.split(":");

            ResourceLocation entityResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);

            EntityType entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityResourceLocation)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Entity: " + entity));

            SpawnerEntityToReplace.add(entityType);
        }
    }

    public void SetLootTable(LootTable lootTable, boolean isOminous){
        if(isOminous)
            SpawnerOminousLootTable = lootTable;
        else
            SpawnerLootTable = lootTable;
    }

    public void SetListOfPokemonToSpawn(List<SpawnablePokemonSettings> spawnablePokemonList, boolean isOminous){
        var newSpawnablePokemonList = new SimpleWeightedRandomList.Builder<PokemonProperties>();

        for(SpawnablePokemonSettings pokemonSettings: spawnablePokemonList){
            PokemonProperties pokemon = pokemonSettings.getSpawnablePokemonProperties();
            newSpawnablePokemonList.add(pokemon, pokemonSettings.getSpawnWeight());
        }

        if(isOminous)
            ListOfOminousPokemonToSpawn = newSpawnablePokemonList.build();
        else
            ListOfPokemonToSpawn = newSpawnablePokemonList.build();
    }

    public boolean DoesSpawnerSettingsContainEntityToReplace(EntityType entityType){
        return SpawnerEntityToReplace.contains(entityType);
    }

    public boolean DoesSpawnerSettingsContainSpawnerTypesToReplace(BlockEntityType blockEntityType){
        return SpawnerTypesToReplace.contains(blockEntityType);
    }


    public int getTicksBetweenSpawnAttempts() {
        return TicksBetweenSpawnAttempts;
    }

    public int getSpawnerCooldown() {
        return SpawnerCooldown;
    }

    public int getPlayerDetectionRange() {
        return PlayerDetectionRange;
    }

    public int getSpawnRange() {
        return SpawnRange;
    }

    public int getMaximumNumberOfSimultaneousPokemon() {
        return MaximumNumberOfSimultaneousPokemon;
    }

    public int getMaximumNumberOfSimultaneousPokemonAddedPerPlayer() {
        return MaximumNumberOfSimultaneousPokemonAddedPerPlayer;
    }

    public int getTotalNumberOfPokemonPerTrial() {
        return TotalNumberOfPokemonPerTrial;
    }

    public int getTotalNumberOfPokemonPerTrialAddedPerPlayer() {
        return TotalNumberOfPokemonPerTrialAddedPerPlayer;
    }

    public LootTable getSpawnerLootTable() {
        return SpawnerLootTable;
    }

    public LootTable getSpawnerOminousLootTable() {
        return SpawnerOminousLootTable;
    }

    public boolean isDisableOminousSpawnerAttacks() {
        return DisableOminousSpawnerAttacks;
    }

    public SimpleWeightedRandomList<PokemonProperties> getListOfPokemonToSpawn() {
        return ListOfPokemonToSpawn;
    }

    public SimpleWeightedRandomList<PokemonProperties> getListOfOminousPokemonToSpawn() {
        return ListOfOminousPokemonToSpawn;
    }
}
