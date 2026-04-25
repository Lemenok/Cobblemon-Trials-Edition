package com.lemenok.cobblemontrialsedition.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class CobblemonTrailDespawnMixin extends LivingEntity {

    private static final Logger LOGGER = LogManager.getLogger("cobblemontrialsedition");
    // TODO: Make Timeout configurable.
    private static final int FIVE_MINUTES_IN_TICKS = 6000;

    protected CobblemonTrailDespawnMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleTrialDespawn(CallbackInfo callbackInfo) {
        if(!this.level().isClientSide() && this.level().getGameTime() % 20 == 0) {

            PokemonEntity pokemon = (PokemonEntity) (Object) this;
            CompoundTag data = pokemon.getPokemon().getPersistentData();

            if (data.getBoolean("is_spawned_from_trial_spawner")){
                if (this.tickCount >= FIVE_MINUTES_IN_TICKS) {
                    LOGGER.info("Removed Trial Spawner Pokemon: {}, Timed out after 5 minutes.", pokemon.getName().getString());
                    this.discard();
                }
            }
        }
    }
}

