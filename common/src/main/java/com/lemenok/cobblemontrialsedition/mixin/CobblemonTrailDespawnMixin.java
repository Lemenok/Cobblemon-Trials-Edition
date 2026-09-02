package com.lemenok.cobblemontrialsedition.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class CobblemonTrailDespawnMixin extends LivingEntity {

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("cobblemontrialsedition");

    @Unique
    private boolean cobblemonTrials$isChecked = false;

    @Unique
    private boolean cobblemonTrials$isTrialSpawned = false;

    @Unique
    private long cobblemonTrials$spawnerPos = 0L;

    protected CobblemonTrailDespawnMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void handleTrialDespawn(CallbackInfo callbackInfo) {
        if (!this.level().isClientSide() && this.level().getGameTime() % 20 == 0) {

            // 1. Lazy-load the NBT data to avoid constant polling
            if (!this.cobblemonTrials$isChecked) {
                PokemonEntity pokemon = (PokemonEntity) (Object) this;
                CompoundTag data = pokemon.getPokemon().getPersistentData();

                this.cobblemonTrials$isTrialSpawned = data.getBoolean("is_spawned_from_trial_spawner");
                if (data.contains("spawner_pos")) {
                    this.cobblemonTrials$spawnerPos = data.getLong("spawner_pos");
                }
                this.cobblemonTrials$isChecked = true;
            }

            // 2. Evaluate timeout and reset spawner
            if (this.cobblemonTrials$isTrialSpawned) {
                if (this.tickCount >= Services.PLATFORM.getCommonConfig().TIME_TILL_POKEMON_DESPAWN_IN_TICKS) {

                    BlockPos spawnerPos = BlockPos.of(this.cobblemonTrials$spawnerPos);
                    BlockEntity blockEntity = this.level().getBlockEntity(spawnerPos);

                    // Trigger the explicit reset before discarding the entity
                    if (blockEntity instanceof CobblemonTrialSpawnerEntity trialSpawner) {
                        // Ensure resetSpawnerData is made public in CobblemonTrialSpawnerEntity
                        trialSpawner.resetSpawnerData(trialSpawner.getCobblemonTrialSpawner().getData(), trialSpawner.getCobblemonTrialSpawner());
                    }

                    PokemonEntity pokemon = (PokemonEntity) (Object) this;
                    LOGGER.info("Removed Trial Spawner Pokemon: {}, Timed out after 5 minutes.", pokemon.getName().getString());
                    this.discard();
                }
            }
        }
    }
}

