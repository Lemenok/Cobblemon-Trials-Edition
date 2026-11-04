package com.lemenok.cobblemontrialsedition.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record PokemonSpawnConfig(
        boolean isWaveMode,
        List<SpawnablePokemonProperties> listOfPokemonToSpawn,
        List<SpawnablePokemonProperties> listOfOminousPokemonToSpawn,
        List<SpawnerProperties.PokemonWaveDefinition> listOfPokemonWaves,
        List<SpawnerProperties.PokemonWaveDefinition> listOfOminousPokemonWaves
) {
    public static final Codec<PokemonSpawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("isWaveMode", false).forGetter(PokemonSpawnConfig::isWaveMode),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfPokemonToSpawn", new ArrayList<>()).forGetter(PokemonSpawnConfig::listOfPokemonToSpawn),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfOminousPokemonToSpawn", new ArrayList<>()).forGetter(PokemonSpawnConfig::listOfOminousPokemonToSpawn),
            Codec.list(SpawnerProperties.PokemonWaveDefinition.CODEC).optionalFieldOf("listOfPokemonWaves", new ArrayList<>()).forGetter(PokemonSpawnConfig::listOfPokemonWaves),
            Codec.list(SpawnerProperties.PokemonWaveDefinition.CODEC).optionalFieldOf("listOfOminousPokemonWaves", new ArrayList<>()).forGetter(PokemonSpawnConfig::listOfOminousPokemonWaves)
    ).apply(instance, PokemonSpawnConfig::new));
}
