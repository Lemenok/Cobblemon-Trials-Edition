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
import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SpawnData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public record SpawnablePokemonProperties(
        String species,
        int weight,
        String form,
        int level,
        String gender,
        String nature,
        List<Integer> defaultEVs,
        List<Integer> defaultIVs,
        String ability,
        int dynaMaxLevel,
        String teraType,
        boolean isShiny,
        float scaleModifier,
        boolean isUncatchable,
        boolean mustBeDefeatedInBattle
)
{
    public static final Codec<SpawnablePokemonProperties> CODEC = RecordCodecBuilder.create(pokemon -> pokemon.group(
            Codec.STRING.fieldOf("species").forGetter(SpawnablePokemonProperties::species),
            Codec.INT.fieldOf("weight").forGetter(SpawnablePokemonProperties::weight),
            Codec.STRING.optionalFieldOf("form", "normal").forGetter(SpawnablePokemonProperties::form),
            Codec.INT.fieldOf("level").forGetter(SpawnablePokemonProperties::level),
            Codec.STRING.optionalFieldOf("gender", "").forGetter(SpawnablePokemonProperties::gender),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(SpawnablePokemonProperties::nature),
            Codec.list(Codec.INT).optionalFieldOf("defaultEVs", new ArrayList<>()).forGetter(SpawnablePokemonProperties::defaultEVs),
            Codec.list(Codec.INT).optionalFieldOf("defaultIVs", new ArrayList<>()).forGetter(SpawnablePokemonProperties::defaultIVs),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(SpawnablePokemonProperties::ability),
            Codec.INT.optionalFieldOf("dynaMaxLevel", 0).forGetter(SpawnablePokemonProperties::dynaMaxLevel),
            Codec.STRING.optionalFieldOf("teraType", "").forGetter(SpawnablePokemonProperties::teraType),
            Codec.BOOL.optionalFieldOf("isShiny", false).forGetter(SpawnablePokemonProperties::isShiny),
            Codec.FLOAT.optionalFieldOf("scaleModifier", 1.0f).forGetter(SpawnablePokemonProperties::scaleModifier),
            Codec.BOOL.optionalFieldOf("isUncatchable", true).forGetter(SpawnablePokemonProperties::isUncatchable),
            Codec.BOOL.optionalFieldOf("mustBeDefeatedInBattle", true).forGetter(SpawnablePokemonProperties::mustBeDefeatedInBattle)
    ).apply(pokemon, SpawnablePokemonProperties::new));

    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEditionFabric.MODID);

    public SpawnData getPokemonSpawnData(ServerLevel serverLevel, boolean doPokemonSpawnedGlow) {

        if(Config.ENABLE_DEBUG_LOGS.get()){
            LOGGER.info("Setting up spawn data for '{}'", this.species);
        }

        PokemonProperties newPokemonProperties = getSpawnablePokemonProperties();
        Pokemon newPokemon = newPokemonProperties.create();

        List<SpeciesFeature> speciesFeature = new ArrayList<>();
        speciesFeature.add(new FlagSpeciesFeature(form,true));

        if(form.equalsIgnoreCase("mega"))
            speciesFeature.add(new StringSpeciesFeature("mega_evolution","mega"));

        if(form.equalsIgnoreCase("gmax"))
            speciesFeature.add(new StringSpeciesFeature("dynamax_form","gmax"));

        newPokemon.setFeatures(speciesFeature);

        newPokemon.setScaleModifier(scaleModifier);

        CompoundTag pokemonNbt = newPokemon.saveToNBT(serverLevel.registryAccess(), new CompoundTag());

        if(!Config.ALLOW_SPAWNED_POKEMON_TO_BE_CATCHABLE.get()){
            if(isUncatchable){
                // Make pokemon uncatchable
                String[] data = new String[] { "uncatchable", "uncatchable", "uncatchable" };
                ListTag listTag = new ListTag();
                for (String stringData : data) { listTag.add(StringTag.valueOf(stringData)); }
                pokemonNbt.put("PokemonData", listTag);
            }
        }

        CompoundTag entityNbt = new CompoundTag();
        entityNbt.put("Pokemon", pokemonNbt);
        entityNbt.putString("id", "cobblemon:pokemon");
        entityNbt.putString("PoseType", "WALK");
        if(doPokemonSpawnedGlow) entityNbt.putByte("Glowing", (byte) 1);

        if(!Config.ALLOW_SPAWNED_POKEMON_TO_BE_DEFEATED_OUTSIDE_OF_BATTLE.get()){
            if(mustBeDefeatedInBattle){
                entityNbt.putBoolean("Invulnerable", true);
            }
        }


        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entityNbt);

        DataResult<SpawnData> result = SpawnData.CODEC.parse(NbtOps.INSTANCE, spawnData);
        return result.getOrThrow();
    }

    private PokemonProperties getSpawnablePokemonProperties() {
        PokemonProperties pokemonProperties = new PokemonProperties();
        pokemonProperties.setSpecies(species);
        pokemonProperties.setForm(form);
        pokemonProperties.setLevel(level);
        pokemonProperties.setGender(parseGender(gender));
        pokemonProperties.setNature(parseNature(nature).getDisplayName());
        pokemonProperties.setEvs(parseEVs(defaultEVs));
        pokemonProperties.setIvs(parseIVs(defaultIVs));
        pokemonProperties.setShiny(isShiny);
        pokemonProperties.setDmaxLevel(dynaMaxLevel);
        pokemonProperties.setTeraType(parseTeraType(teraType));
        setAbility(pokemonProperties, ability);

        return pokemonProperties;
    }


    private Gender parseGender(String genderString){
        if (genderString == null || genderString.isEmpty()) {
            return Gender.GENDERLESS;
        }

        return switch (genderString.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            default -> Gender.GENDERLESS;
        };
    }

    private static Nature parseNature(String nature){
        Nature pokemonNature = Natures.getNature(nature);

        if(pokemonNature != null) {
            return pokemonNature;
        }

        return Natures.getRandomNature();
    }

    private void setAbility(PokemonProperties pokemonProperties, String ability){
        if (ability.isEmpty()) return;

        pokemonProperties.setAbility(Abilities.getOrException(ability).getName());
    }

    private EVs parseEVs (List<Integer> defaultEVs){
        EVs evs = new EVs();

        // Ensure EVs have exactly 6 numbers, if not return blank signifiying random.
        if(this.defaultEVs.size() != 6)
            return evs;

        // Ensure Evs are within the possible range to be set.
        for (int ev: this.defaultEVs){
            if(ev < 0 || ev > 252)
                return evs;
        }

        // Ensure Ev totals are equal to or less than 510.
        int evTotal = 0;
        for (int ev: this.defaultEVs){
            evTotal = evTotal + ev;
            if(evTotal > 510)
                return evs;
        }

        evs.set(Stats.HP, this.defaultEVs.get(0));
        evs.set(Stats.ATTACK, this.defaultEVs.get(1));
        evs.set(Stats.DEFENCE, this.defaultEVs.get(2));
        evs.set(Stats.SPECIAL_ATTACK, this.defaultEVs.get(3));
        evs.set(Stats.SPECIAL_DEFENCE, this.defaultEVs.get(4));
        evs.set(Stats.SPEED, this.defaultEVs.get(5));

        return evs;
    }

    private IVs parseIVs (List<Integer> defaultIVs){
        IVs ivs = new IVs();

        // Ensure IVs have exactly 6 numbers, if not return blank signifiying random.
        if(defaultIVs.size() != 6)
            return ivs;

        // Ensure Ivs are within the possible range to be set.
        for (int iv: this.defaultIVs){
            if(iv < 0 || iv > 31)
                return ivs;
        }

        ivs.set(Stats.HP, defaultIVs.get(0));
        ivs.set(Stats.ATTACK, defaultIVs.get(1));
        ivs.set(Stats.DEFENCE, defaultIVs.get(2));
        ivs.set(Stats.SPECIAL_ATTACK, defaultIVs.get(3));
        ivs.set(Stats.SPECIAL_DEFENCE, defaultIVs.get(4));
        ivs.set(Stats.SPEED, defaultIVs.get(5));

        return ivs;
    }

    private String parseTeraType(String teraType) {
        if(teraType.isEmpty()){
            return TeraTypes.random(true).showdownId();
        }

        return Objects.requireNonNull(TeraTypes.get(teraType)).showdownId();
    }
}
