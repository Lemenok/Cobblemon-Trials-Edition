package com.lemenok.cobblemontrialsedition.config.mappers;

public class SpawnerMapper {
    public String[] SpawnerTypeToReplace;
    public String[] SpawnerEntityToReplace;
    public int TicksBetweenSpawnAttempts;
    public int SpawnerCooldown;
    public int PlayerDetectionRange;
    public int SpawnRange;
    public int MaximumNumberOfSimultaneousPokemon;
    public int MaximumNumberOfSimultaneousPokemonAddedPerPlayer;
    public int TotalNumberOfPokemonPerTrial;
    public int TotalNumberOfPokemonPerTrialAddedPerPlayer;
    public String LootTable;
    public String OminousLootTable;
    public boolean OminousSpawnerAttacksEnabled;
    public boolean DoPokemonSpawnedGlow;
    public SpawnablePokemonMapper[] ListOfPokemonToSpawn;
    public SpawnablePokemonMapper[] ListOfOminousPokemonToSpawn;
}
