package com.lemenok.cobblemontrialsedition.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class CobblemonTrailDespawnMixin extends LivingEntity {

    private static final Logger LOGGER = LogManager.getLogger("cobblemontrialsedition");

    protected CobblemonTrailDespawnMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleTrialDespawn(CallbackInfo callbackInfo) {
        if(!this.level().isClientSide() && this.level().getGameTime() % 20 == 0) {

            PokemonEntity pokemon = (PokemonEntity) (Object) this;
            CompoundTag data = pokemon.getPokemon().getPersistentData();

            if (data.getBoolean("cobblemon_trials_edition_is_aggressive")){
                boolean playerNearby = this.level().hasNearbyAlivePlayer(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        64.0
                );

                if(!playerNearby) {
                    LOGGER.info("Removed Pokemon due to no players being nearby.");
                    this.discard();
                }
            }
        }
    }
}

