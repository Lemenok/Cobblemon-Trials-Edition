package com.lemenok.cobblemontrialsedition.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record SpawnConfig(
        boolean isWaveMode,
        List<SpawnablePokemonProperties> listOfPokemonToSpawn,
        List<SpawnablePokemonProperties> listOfOminousPokemonToSpawn,
        List<SpawnerProperties.WaveDefinition> waves,
        List<SpawnerProperties.WaveDefinition> ominousWaves
) {
    public static final Codec<SpawnConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("isWaveMode", false).forGetter(SpawnConfig::isWaveMode),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfPokemonToSpawn", new ArrayList<>()).forGetter(SpawnConfig::listOfPokemonToSpawn),
            Codec.list(SpawnablePokemonProperties.CODEC).optionalFieldOf("listOfOminousPokemonToSpawn", new ArrayList<>()).forGetter(SpawnConfig::listOfOminousPokemonToSpawn),
            Codec.list(SpawnerProperties.WaveDefinition.CODEC).optionalFieldOf("waves", new ArrayList<>()).forGetter(SpawnConfig::waves),
            Codec.list(SpawnerProperties.WaveDefinition.CODEC).optionalFieldOf("ominousWaves", new ArrayList<>()).forGetter(SpawnConfig::ominousWaves)
    ).apply(instance, SpawnConfig::new));
}
