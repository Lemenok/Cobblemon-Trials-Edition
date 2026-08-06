package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.pokemon.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SpawnablePokemonStats(
        String form,
        int level,
        String gender,
        String nature,
        List<Integer> defaultEVs,
        List<Integer> defaultIVs,
        String ability,
        List<String> moves,
        String heldItem,
        int dynaMaxLevel,
        String teraType,
        boolean isShiny
) {
    public static final Codec<SpawnablePokemonStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("form", "normal").forGetter(SpawnablePokemonStats::form),
            Codec.INT.optionalFieldOf("level", 25).forGetter(SpawnablePokemonStats::level),
            Codec.STRING.optionalFieldOf("gender", "").forGetter(SpawnablePokemonStats::gender),
            Codec.STRING.optionalFieldOf("nature", "").forGetter(SpawnablePokemonStats::nature),
            Codec.list(Codec.INT).optionalFieldOf("defaultEVs", new ArrayList<>()).forGetter(SpawnablePokemonStats::defaultEVs),
            Codec.list(Codec.INT).optionalFieldOf("defaultIVs", new ArrayList<>()).forGetter(SpawnablePokemonStats::defaultIVs),
            Codec.STRING.optionalFieldOf("ability", "").forGetter(SpawnablePokemonStats::ability),
            Codec.STRING.listOf().optionalFieldOf("moves", new ArrayList<>()).forGetter(SpawnablePokemonStats::moves),
            Codec.STRING.optionalFieldOf("heldItem", "").forGetter(SpawnablePokemonStats::heldItem),
            Codec.INT.optionalFieldOf("dynaMaxLevel", 0).forGetter(SpawnablePokemonStats::dynaMaxLevel),
            Codec.STRING.optionalFieldOf("teraType", "").forGetter(SpawnablePokemonStats::teraType),
            Codec.BOOL.optionalFieldOf("isShiny", false).forGetter(SpawnablePokemonStats::isShiny)
    ).apply(instance, SpawnablePokemonStats::new));

    public EVs parseEVs (){
        EVs evs = new EVs();

        // Ensure EVs have exactly 6 numbers, if not return blank signifiying random.
        if(this.defaultEVs().size() != 6)
            return evs;

        // Ensure Evs are within the possible range to be set.
        for (int ev: this.defaultEVs()){
            if(ev < 0 || ev > 252)
                return evs;
        }

        // Ensure Ev totals are equal to or less than 510.
        int evTotal = 0;
        for (int ev: this.defaultEVs()){
            evTotal = evTotal + ev;
            if(evTotal > 510)
                return evs;
        }

        evs.set(Stats.HP, this.defaultEVs().get(0));
        evs.set(Stats.ATTACK, this.defaultEVs().get(1));
        evs.set(Stats.DEFENCE, this.defaultEVs().get(2));
        evs.set(Stats.SPECIAL_ATTACK, this.defaultEVs().get(3));
        evs.set(Stats.SPECIAL_DEFENCE, this.defaultEVs().get(4));
        evs.set(Stats.SPEED, this.defaultEVs().get(5));

        return evs;
    }

    public IVs parseIVs (){
        IVs ivs = new IVs();

        // Ensure IVs have exactly 6 numbers, if not return blank signifiying random.
        if(defaultIVs.size() != 6)
            return ivs;

        // Ensure Ivs are within the possible range to be set.
        for (int iv: this.defaultIVs()){
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

    public Gender parseGender(){
        if (gender == null || gender.isEmpty()) {
            return Gender.GENDERLESS;
        }

        return switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            default -> Gender.GENDERLESS;
        };
    }

    public Nature parseNature(){
        Nature pokemonNature = Natures.getNature(nature);

        if(pokemonNature != null) {
            return pokemonNature;
        }

        return Natures.getRandomNature();
    }

    public String getAbility(){
        if (this.ability.isEmpty()) return null;

        return Abilities.getOrException(ability).getName();
    }

    public ItemStack getHeldItem(){
        ResourceLocation location = ResourceLocation.tryParse(heldItem);
        if (location != null && BuiltInRegistries.ITEM.containsKey(location)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(location));
        }

        return ItemStack.EMPTY;
    }

    public String parseTeraType() {
        if(teraType.isEmpty()){
            return TeraTypes.random(true).showdownId();
        }

        return Objects.requireNonNull(TeraTypes.get(teraType)).showdownId();
    }
}
