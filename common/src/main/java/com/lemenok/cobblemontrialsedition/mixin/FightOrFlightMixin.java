package com.lemenok.cobblemontrialsedition.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CobblemonFightOrFlight.class, remap = false)
public class FightOrFlightMixin {

    @Inject(method = "getFightOrFlightCoefficient", at = @At("HEAD"), cancellable = true)
    private static void injectAggression(PokemonEntity pokemonEntity, CallbackInfoReturnable<Double> callbackInfoReturnable) {

        CompoundTag compoundTagOfPokemon = pokemonEntity.getPokemon().getPersistentData();

        if (compoundTagOfPokemon.getBoolean("cobblemon_trials_edition_is_aggressive")) {
            callbackInfoReturnable.setReturnValue(100.0F + (double) CobblemonFightOrFlight.commonConfig().aggressive_threshold);
        }
    }
}
