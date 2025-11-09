package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.pokemon.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnablePokemonSettings {
    private final PokemonProperties SpawnablePokemonProperties;
    private final int SpawnWeight;

    private final int MinimumLevel;
    private final int MaximumLevel;
    private final List<Nature> ListOfNatures;
    private final List<String> ListOfAbilities;
    private final float ScaleModifier;
    private final boolean IsUncatchable;
    private final boolean MustBeDefeatedInBattle;

    public SpawnablePokemonSettings(String species, int spawnWeight, String form, int minimumLevel, int maximumLevel,
                                    String gender, String[] arrayOfNatures, int[] defaultEVs, int[] defaultIVs, String[] arrayOfAbilities,
                                    int dynaMaxLevel, String teraType, boolean isShiny, int scaleModifier, boolean isUncatchable,
                                    boolean mustBeDefeatedInBattle) {

        this.SpawnWeight = spawnWeight;
        this.MinimumLevel = minimumLevel;
        this.MaximumLevel = maximumLevel;
        this.ListOfNatures = parseListOfNatures(arrayOfNatures);
        this.ListOfAbilities = parseListOfAbilities(arrayOfAbilities);
        this.ScaleModifier = scaleModifier;
        this.IsUncatchable = isUncatchable;
        this.MustBeDefeatedInBattle = mustBeDefeatedInBattle;

        PokemonProperties pokemonProperties = new PokemonProperties();
        pokemonProperties.setSpecies(species);
        pokemonProperties.setForm(form);
        pokemonProperties.setGender(parseGender(gender));
        pokemonProperties.setEvs(parseEVs(defaultEVs));
        pokemonProperties.setIvs(parseIVs(defaultIVs));
        pokemonProperties.setShiny(isShiny);
        pokemonProperties.setDmaxLevel(dynaMaxLevel);
        pokemonProperties.setTeraType(parseTeraType(teraType));

        this.SpawnablePokemonProperties = pokemonProperties;
    }

    public PokemonProperties getSpawnablePokemonProperties() {
        return SpawnablePokemonProperties;
    }

    public int getSpawnWeight() {
        return SpawnWeight;
    }

    public int getMinimumLevel() {
        return MinimumLevel;
    }

    public int getMaximumLevel() {
        return MaximumLevel;
    }

    public List<Nature> getListOfNatures() {
        return ListOfNatures;
    }

    public List<String> getListOfAbilities() {
        return ListOfAbilities;
    }

    public float getScaleModifier() {
        return ScaleModifier;
    }

    public boolean isUncatchable() {
        return IsUncatchable;
    }

    public boolean isMustBeDefeatedInBattle() {
        return MustBeDefeatedInBattle;
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

    private List<Nature> parseListOfNatures(String[] arrayOfNatures){

        if(arrayOfNatures.length == 0)
            return List.of();

        List<Nature> parsedNatureList = new ArrayList<>(List.of());

        for(String nature: arrayOfNatures){
            Nature newNature = Natures.INSTANCE.getNature(nature);
            if(newNature != null)
                parsedNatureList.add(newNature);
        }

        return parsedNatureList;
    }

    private List<String> parseListOfAbilities(String[] arrayOfAbilities){
        if(arrayOfAbilities.length == 0)
            return List.of();

        List<String> parsedAbilityList = new ArrayList<>(List.of());

        for(String ability : arrayOfAbilities){
            AbilityTemplate newAbility = Abilities.INSTANCE.getOrException(ability);
            parsedAbilityList.add(newAbility.getName());
        }

        return parsedAbilityList;
    }

    private EVs parseEVs (int[] defaultEVs){
        EVs evs = new EVs();

        if(defaultEVs.length == 0 || defaultEVs.length < 6)
            return evs;

        evs.set(Stats.HP, defaultEVs[0]);
        evs.set(Stats.ATTACK, defaultEVs[1]);
        evs.set(Stats.DEFENCE, defaultEVs[2]);
        evs.set(Stats.SPECIAL_ATTACK, defaultEVs[3]);
        evs.set(Stats.SPECIAL_DEFENCE, defaultEVs[4]);
        evs.set(Stats.SPEED, defaultEVs[5]);

        return evs;
    }

    private IVs parseIVs (int[] defaultIVs){
        IVs ivs = new IVs();

        if(defaultIVs.length == 0 || defaultIVs.length < 6)
            return ivs;

        ivs.set(Stats.HP, defaultIVs[0]);
        ivs.set(Stats.ATTACK, defaultIVs[1]);
        ivs.set(Stats.DEFENCE, defaultIVs[2]);
        ivs.set(Stats.SPECIAL_ATTACK, defaultIVs[3]);
        ivs.set(Stats.SPECIAL_DEFENCE, defaultIVs[4]);
        ivs.set(Stats.SPEED, defaultIVs[5]);

        return ivs;
    }

    private @Nullable String parseTeraType(String teraType) {
        return TeraTypes.get(teraType).showdownId();
    }
}
