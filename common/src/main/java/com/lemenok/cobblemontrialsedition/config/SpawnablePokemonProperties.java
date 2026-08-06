package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
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
import net.minecraft.world.level.SpawnData;
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
            Codec.INT.optionalFieldOf("weight", 10).forGetter(SpawnablePokemonProperties::weight),
            Codec.FLOAT.optionalFieldOf("scaleModifier", 1.0f).forGetter(SpawnablePokemonProperties::scaleModifier),
            Codec.BOOL.optionalFieldOf("isUncatchable", false).forGetter(SpawnablePokemonProperties::isUncatchable),
            Codec.BOOL.optionalFieldOf("mustBeDefeatedInBattle", false).forGetter(SpawnablePokemonProperties::mustBeDefeatedInBattle),
            Codec.BOOL.optionalFieldOf("isAggressive", false).forGetter(SpawnablePokemonProperties::isAggressive),
            Codec.STRING.listOf().optionalFieldOf("aspects", new ArrayList<>()).forGetter(SpawnablePokemonProperties::aspects),
            SpawnablePokemonStats.CODEC.optionalFieldOf("spawnablePokemonStats",
                    new SpawnablePokemonStats("normal", 25, "", "", new ArrayList<>(), new ArrayList<>(), "", new ArrayList<>(), "", 0, "", false)
            ).forGetter(SpawnablePokemonProperties::spawnablePokemonStats)
    ).apply(pokemon, SpawnablePokemonProperties::new));

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    public SpawnData getPokemonSpawnData(RegistryAccess registryAccess, boolean doPokemonSpawnedGlow) {

        if(Services.PLATFORM.getCommonConfig().ENABLE_DEBUG_LOGS){
            LOGGER.info("Setting up spawn data for '{}'", species);
        }

        return buildSpawnData(registryAccess, doPokemonSpawnedGlow);
    }

    private SpawnData buildSpawnData(RegistryAccess registryAccess, Boolean doPokemonSpawnedGlow) {
        PokemonProperties newPokemonProperties = getSpawnablePokemonProperties();
        Pokemon newPokemon = newPokemonProperties.create();

        // Set aspects from players
        newPokemon.setForcedAspects(new HashSet<>(aspects));

        newPokemon.setHeldItem$common(spawnablePokemonStats.getHeldItem());

        newPokemon.getPersistentData().putBoolean("is_spawned_from_trial_spawner", true);

        List<SpeciesFeature> speciesFeature = new ArrayList<>();
        speciesFeature.add(new FlagSpeciesFeature(spawnablePokemonStats.form(),true));

        manageForms(speciesFeature);

        newPokemon.setFeatures(speciesFeature);

        newPokemon.setScaleModifier(scaleModifier);

        newPokemon.getPersistentData().putBoolean("is_aggressive", isAggressive);

        newPokemon.getPersistentData().putBoolean("is_uncatchable", isUncatchable);

        newPokemon.getPersistentData().putBoolean("is_invulnerable", mustBeDefeatedInBattle);

        if(!spawnablePokemonStats.moves().isEmpty()){
            // Setup Custom Moves
            ListTag moveListTag = new ListTag();
            for (String stringData : spawnablePokemonStats.moves()) { moveListTag.add(StringTag.valueOf(stringData)); }
            newPokemon.getPersistentData().put("custom_moves", moveListTag);
        }

        CompoundTag pokemonNbt = newPokemon.saveToNBT(registryAccess, new CompoundTag());

        CompoundTag entityNbt = new CompoundTag();
        entityNbt.put("Pokemon", pokemonNbt);
        entityNbt.putString("id", "cobblemon:pokemon");
        entityNbt.putString("PoseType", "WALK");
        if(doPokemonSpawnedGlow) entityNbt.putByte("Glowing", (byte) 1);

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
        pokemonProperties.setShiny(spawnablePokemonStats.isShiny());
        pokemonProperties.setDmaxLevel(spawnablePokemonStats.dynaMaxLevel());
        pokemonProperties.setTeraType(spawnablePokemonStats.parseTeraType());

        String ability = spawnablePokemonStats.getAbility();
        if(ability != null)
            pokemonProperties.setAbility(ability);

        return pokemonProperties;
    }
}
