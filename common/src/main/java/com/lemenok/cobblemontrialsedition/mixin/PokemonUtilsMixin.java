package com.lemenok.cobblemontrialsedition.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.rufia.fightorflight.utils.PokemonUtils;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PokemonUtils.class, remap = false)
public class PokemonUtilsMixin {

    @Inject(method = "WildPokemonCanPerformUnprovokedAttack", at = @At("HEAD"), cancellable = true)
    private static void injectUnprovokedAttack(PokemonEntity pokemonEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        CompoundTag compoundTagOfPokemon = pokemonEntity.getPokemon().getPersistentData();
        callbackInfoReturnable.setReturnValue(compoundTagOfPokemon.getBoolean("is_aggressive"));
    }
}