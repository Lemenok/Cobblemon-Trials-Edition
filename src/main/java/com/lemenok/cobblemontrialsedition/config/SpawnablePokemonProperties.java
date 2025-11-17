package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.pokemon.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SpawnData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        int scaleModifier,
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
            Codec.INT.optionalFieldOf("scaleModifier", 0).forGetter(SpawnablePokemonProperties::scaleModifier),
            Codec.BOOL.optionalFieldOf("isUncatchable", true).forGetter(SpawnablePokemonProperties::isUncatchable),
            Codec.BOOL.optionalFieldOf("mustBeDefeatedInBattle", true).forGetter(SpawnablePokemonProperties::mustBeDefeatedInBattle)
    ).apply(pokemon, SpawnablePokemonProperties::new));

    public SpawnData getPokemonSpawnData(ServerLevel serverLevel, boolean doPokemonSpawnedGlow) {
        
        PokemonProperties newPokemonProperties = getSpawnablePokemonProperties();
        Pokemon newPokemon = newPokemonProperties.create();
        newPokemon.setScaleModifier(scaleModifier);

        CompoundTag pokemonNbt = newPokemon.saveToNBT(serverLevel.registryAccess(), new CompoundTag());

        if(isUncatchable){
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

        if(mustBeDefeatedInBattle){
            entityNbt.putBoolean("Invulnerable", true);
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

    private Nature parseNature(String nature){
        return nature.isEmpty() ? Natures.INSTANCE.getRandomNature() : Natures.INSTANCE.getNature(nature);
    }

    private void setAbility(PokemonProperties pokemonProperties, String ability){
        if (ability.isEmpty()) return;
        pokemonProperties.setAbility(Abilities.INSTANCE.getOrException(ability).getName());
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

    private @Nullable String parseTeraType(String teraType) {
        return TeraTypes.get(teraType).showdownId();
    }
}
