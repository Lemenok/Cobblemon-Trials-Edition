package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.pokemon.*;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SpawnData;
import org.apache.http.annotation.Obsolete;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public record SpawnablePokemonProperties(
        String species,
        int weight,
        float scaleModifier,
        boolean isUncatchable,
        boolean mustBeDefeatedInBattle,
        boolean isAggressive,
        List<String> aspects,
        SpawnablePokemonStats spawnablePokemonStats
)
{
    public static final Codec<SpawnablePokemonProperties> CODEC = RecordCodecBuilder.create(pokemon -> pokemon.group(
            Codec.STRING.fieldOf("species").forGetter(SpawnablePokemonProperties::species),
            Codec.INT.fieldOf("weight").forGetter(SpawnablePokemonProperties::weight),
            Codec.FLOAT.optionalFieldOf("scaleModifier", 1.0f).forGetter(SpawnablePokemonProperties::scaleModifier),
            Codec.BOOL.optionalFieldOf("isUncatchable", false).forGetter(SpawnablePokemonProperties::isUncatchable),
            Codec.BOOL.optionalFieldOf("mustBeDefeatedInBattle", false).forGetter(SpawnablePokemonProperties::mustBeDefeatedInBattle),
            Codec.BOOL.optionalFieldOf("isAggressive", true).forGetter(SpawnablePokemonProperties::isAggressive),
            Codec.STRING.listOf().optionalFieldOf("aspects", new ArrayList<>()).forGetter(SpawnablePokemonProperties::aspects),
            SpawnablePokemonStats.CODEC.optionalFieldOf("spawnablePokemonStats",
                    new SpawnablePokemonStats("normal", 25, "", "", new ArrayList<>(), new ArrayList<>(), "", new ArrayList<>(), 0, "", false)
            ).forGetter(SpawnablePokemonProperties::spawnablePokemonStats)
    ).apply(pokemon, SpawnablePokemonProperties::new));

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    public SpawnData getPokemonSpawnData(RegistryAccess registryAccess, boolean doPokemonSpawnedGlow) {

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS){
            LOGGER.info("Setting up spawn data for '{}'", species);
        }

        PokemonProperties newPokemonProperties = getSpawnablePokemonProperties();
        Pokemon newPokemon = newPokemonProperties.create();

        // Set aspects from players
        newPokemon.setForcedAspects(new HashSet<>(aspects));

        newPokemon.getPersistentData().putBoolean("is_spawned_from_trial_spawner", true);

        List<SpeciesFeature> speciesFeature = new ArrayList<>();
        speciesFeature.add(new FlagSpeciesFeature(spawnablePokemonStats.form(),true));

        manageForms(speciesFeature);

        newPokemon.setFeatures(speciesFeature);

        newPokemon.setScaleModifier(scaleModifier);

        if(Services.PLATFORM.getCommonConfig().ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE) {
            newPokemon.getPersistentData().putBoolean("cobblemon_trials_edition_is_aggressive", isAggressive);
        }

        CompoundTag pokemonNbt = newPokemon.saveToNBT(registryAccess, new CompoundTag());

        if(Services.PLATFORM.getCommonConfig().SPAWNED_POKEMON_ARE_UNCATCHABLE || isUncatchable){
            // Make pokemon uncatchable
            String[] data = new String[] { "uncatchable", "uncatchable", "uncatchable" };
            ListTag listTag = new ListTag();
            for (String stringData : data) { listTag.add(StringTag.valueOf(stringData)); }
            pokemonNbt.put("PokemonData", listTag);
        }

        CompoundTag entityNbt = new CompoundTag();
        entityNbt.put("Pokemon", pokemonNbt);
        entityNbt.putString("id", "cobblemon:pokemon");
        entityNbt.putString("PoseType", "WALK");
        if(doPokemonSpawnedGlow) entityNbt.putByte("Glowing", (byte) 1);

        if(Services.PLATFORM.getCommonConfig().SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE || mustBeDefeatedInBattle){
            entityNbt.putBoolean("Invulnerable", true);
        }

        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entityNbt);

        DataResult<SpawnData> result = SpawnData.CODEC.parse(NbtOps.INSTANCE, spawnData);
        return result.getOrThrow();
    }

    private void manageForms(List<SpeciesFeature> speciesFeature) {
        if(spawnablePokemonStats.form().equalsIgnoreCase("mega"))
            speciesFeature.add(new StringSpeciesFeature("mega_evolution","mega"));

        if(spawnablePokemonStats.form().equalsIgnoreCase("gmax"))
            speciesFeature.add(new StringSpeciesFeature("dynamax_form","gmax"));

        // Handle Rotom Forms.
        if(species.equalsIgnoreCase("rotom"))
            speciesFeature.add(new StringSpeciesFeature("appliance",spawnablePokemonStats.form()));
    }

    private PokemonProperties getSpawnablePokemonProperties() {
        PokemonProperties pokemonProperties = new PokemonProperties();
        pokemonProperties.setSpecies(species);
        pokemonProperties.setForm(spawnablePokemonStats.form());
        pokemonProperties.setLevel(spawnablePokemonStats.level());
        pokemonProperties.setGender(spawnablePokemonStats.parseGender());
        pokemonProperties.setNature(spawnablePokemonStats.parseNature().getDisplayName());
        pokemonProperties.setEvs(spawnablePokemonStats.parseEVs());
        pokemonProperties.setIvs(spawnablePokemonStats.parseIVs());
        pokemonProperties.setMoves(spawnablePokemonStats.moves());
        pokemonProperties.setShiny(spawnablePokemonStats.isShiny());
        pokemonProperties.setDmaxLevel(spawnablePokemonStats.dynaMaxLevel());
        pokemonProperties.setTeraType(spawnablePokemonStats.parseTeraType());

        String ability = spawnablePokemonStats.getAbility();
        if(ability != null)
            pokemonProperties.setAbility(ability);

        return pokemonProperties;
    }
}
