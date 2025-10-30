package com.lemenok.cobblemontrialsedition.models;

import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public enum CobblemonTrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, CobblemonTrialSpawnerState.ParticleEmission.NONE, (double)-1.0F, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmission.UNOWN, (double)200.0F, true),
    ACTIVE("active", 8, ParticleEmission.UNOWN_AND_SPARKS, (double)1000.0F, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, CobblemonTrialSpawnerState.ParticleEmission.UNOWN, (double)-1.0F, false),
    EJECTING_REWARD("ejecting_reward", 8, CobblemonTrialSpawnerState.ParticleEmission.UNOWN, (double)-1.0F, false),
    COOLDOWN("cooldown", 0, CobblemonTrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, (double)-1.0F, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final CobblemonTrialSpawnerState.ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private CobblemonTrialSpawnerState(String string2, int j, CobblemonTrialSpawnerState.ParticleEmission arg, double d, boolean bl) {
        this.name = string2;
        this.lightLevel = j;
        this.particleEmission = arg;
        this.spinningMobSpeed = d;
        this.isCapableOfSpawning = bl;
    }

    CobblemonTrialSpawnerState tickAndGetNext(BlockPos arg, CobblemonTrialSpawner arg2, ServerLevel arg3) {
        CobblemonTrialSpawnerData trialSpawnerData = arg2.getData();
        TrialSpawnerConfig trialSpawnerConfig = arg2.getConfig();
        CobblemonTrialSpawnerState var10000;
        switch (this.ordinal()) {
            case 0:
                var10000 = trialSpawnerData.getOrCreateDisplayEntity(false, arg3, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
                break;
            case 1:
                if (!arg2.canSpawnInLevel(arg3)) {
                    trialSpawnerData.reset();
                    var10000 = this;
                } else if (!trialSpawnerData.hasMobToSpawn(arg2, arg3.random)) {
                    var10000 = INACTIVE;
                } else {
                    trialSpawnerData.tryDetectPlayers(arg3, arg, arg2);
                    var10000 = trialSpawnerData.detectedPlayers.isEmpty() ? this : ACTIVE;
                }
                break;
            case 2:
                if (!arg2.canSpawnInLevel(arg3)) {
                    trialSpawnerData.reset();
                    var10000 = WAITING_FOR_PLAYERS;
                } else if (!trialSpawnerData.hasMobToSpawn(arg2, arg3.random)) {
                    var10000 = INACTIVE;
                } else {
                    int i = trialSpawnerData.countAdditionalPlayers(arg);
                    trialSpawnerData.tryDetectPlayers(arg3, arg, arg2);
                    if (arg2.isOminous()) {
                        this.spawnOminousOminousItemSpawner(arg3, arg, arg2);
                    }

                    if (trialSpawnerData.hasFinishedSpawningAllMobs(trialSpawnerConfig, i)) {
                        if (trialSpawnerData.haveAllCurrentMobsDied()) {
                            trialSpawnerData.cooldownEndsAt = arg3.getGameTime() + (long)arg2.getTargetCooldownLength();
                            trialSpawnerData.totalMobsSpawned = 0;
                            trialSpawnerData.nextMobSpawnsAt = 0L;
                            var10000 = WAITING_FOR_REWARD_EJECTION;
                            break;
                        }
                    } else if (trialSpawnerData.isReadyToSpawnNextMob(arg3, trialSpawnerConfig, i)) {
                        arg2.spawnMob(arg3, arg).ifPresent((uUID) -> {
                            trialSpawnerData.currentMobs.add(uUID);
                            ++trialSpawnerData.totalMobsSpawned;
                            trialSpawnerData.nextMobSpawnsAt = arg3.getGameTime() + (long)trialSpawnerConfig.ticksBetweenSpawn();
                            trialSpawnerConfig.spawnPotentialsDefinition().getRandom(arg3.getRandom()).ifPresent((arg3x) -> {
                                trialSpawnerData.nextSpawnData = Optional.of((SpawnData)arg3x.data());
                                arg2.markUpdated();
                            });
                        });
                    }

                    var10000 = this;
                }
                break;
            case 3:
                if (trialSpawnerData.isReadyToOpenShutter(arg3, 40.0F, arg2.getTargetCooldownLength())) {
                    arg3.playSound((Player)null, arg, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
                    var10000 = EJECTING_REWARD;
                } else {
                    var10000 = this;
                }
                break;
            case 4:
                if (!trialSpawnerData.isReadyToEjectItems(arg3, (float)TIME_BETWEEN_EACH_EJECTION, arg2.getTargetCooldownLength())) {
                    var10000 = this;
                } else if (trialSpawnerData.detectedPlayers.isEmpty()) {
                    arg3.playSound((Player)null, arg, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
                    trialSpawnerData.ejectingLootTable = Optional.empty();
                    var10000 = COOLDOWN;
                } else {
                    if (trialSpawnerData.ejectingLootTable.isEmpty()) {
                        trialSpawnerData.ejectingLootTable = trialSpawnerConfig.lootTablesToEject().getRandomValue(arg3.getRandom());
                    }

                    trialSpawnerData.ejectingLootTable.ifPresent((arg4) -> arg2.ejectReward(arg3, arg, arg4));
                    trialSpawnerData.detectedPlayers.remove(trialSpawnerData.detectedPlayers.iterator().next());
                    var10000 = this;
                }
                break;
            case 5:
                trialSpawnerData.tryDetectPlayers(arg3, arg, arg2);
                if (!trialSpawnerData.detectedPlayers.isEmpty()) {
                    trialSpawnerData.totalMobsSpawned = 0;
                    trialSpawnerData.nextMobSpawnsAt = 0L;
                    var10000 = ACTIVE;
                } else if (trialSpawnerData.isCooldownFinished(arg3)) {
                    arg2.removeOminous(arg3, arg);
                    trialSpawnerData.reset();
                    var10000 = WAITING_FOR_PLAYERS;
                } else {
                    var10000 = this;
                }
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return var10000;
    }

    private void spawnOminousOminousItemSpawner(ServerLevel arg, BlockPos arg2, CobblemonTrialSpawner arg3) {
        CobblemonTrialSpawnerData trialSpawnerData = arg3.getData();
        TrialSpawnerConfig trialSpawnerConfig = arg3.getConfig();
        ItemStack itemStack = (ItemStack)trialSpawnerData.getDispensingItems(arg, trialSpawnerConfig, arg2).getRandomValue(arg.random).orElse(ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            if (this.timeToSpawnItemSpawner(arg, trialSpawnerData)) {
                calculatePositionToSpawnSpawner(arg, arg2, arg3, trialSpawnerData).ifPresent((arg5) -> {
                    OminousItemSpawner ominousItemSpawner = OminousItemSpawner.create(arg, itemStack);
                    ominousItemSpawner.moveTo(arg5);
                    arg.addFreshEntity(ominousItemSpawner);
                    float f = (arg.getRandom().nextFloat() - arg.getRandom().nextFloat()) * 0.2F + 1.0F;
                    arg.playSound((Player)null, BlockPos.containing(arg5), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, f);
                    trialSpawnerData.cooldownEndsAt = arg.getGameTime() + arg3.getOminousConfig().ticksBetweenItemSpawners();
                });
            }

        }
    }

    private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel serverLevel, BlockPos blockPosition, CobblemonTrialSpawner cobblemonTrialSpawner, CobblemonTrialSpawnerData cobblemonTrialSpawnerData) {
        List<Player> players = cobblemonTrialSpawnerData.detectedPlayers.stream()
                .map(serverLevel::getPlayerByUUID)
                .filter(Objects::nonNull)
                .filter(player -> {
                    Vec3 center = Vec3.atCenterOf(blockPosition);
                    return !player.isCreative() && !player.isSpectator() && player.isAlive() && player.distanceToSqr(center) <= (double)Mth.square(cobblemonTrialSpawner.getRequiredPlayerRange());
                })
                .toList();

        if (players.isEmpty()) return Optional.empty();

        Entity entity = selectEntityToSpawnItemAbove(players, cobblemonTrialSpawnerData.currentMobs, cobblemonTrialSpawner, blockPosition, serverLevel);
        return entity == null ? Optional.empty() : calculatePositionAbove(entity, serverLevel);
    }

    private static Optional<Vec3> calculatePositionAbove(Entity arg, ServerLevel arg2) {
        Vec3 vec3 = arg.position();
        Vec3 vec32 = vec3.relative(Direction.UP, (double)(arg.getBbHeight() + 2.0F + (float)arg2.random.nextInt(4)));
        BlockHitResult blockHitResult = arg2.clip(new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3 vec33 = blockHitResult.getBlockPos().getCenter().relative(Direction.DOWN, (double)1.0F);
        BlockPos blockPos = BlockPos.containing(vec33);
        return !arg2.getBlockState(blockPos).getCollisionShape(arg2, blockPos).isEmpty() ? Optional.empty() : Optional.of(vec33);
    }

    @Nullable
    private static Entity selectEntityToSpawnItemAbove(List<Player> playerList, Set<UUID> uuidSet, CobblemonTrialSpawner cobblemonTrialSpawner, BlockPos blockPos, ServerLevel serverLevel) {
        Stream<UUID> uuidStream = uuidSet.stream();
        Objects.requireNonNull(serverLevel);

        Stream<Entity> entityStream = uuidStream
                .map(uuid -> serverLevel.getEntity(uuid)) // make call explicit so IDE resolves it
                .filter(Objects::nonNull)
                .filter(entity -> {
                    // ensure the lambda parameter is typed as Entity so isAlive and distanceToSqr resolve
                    if (!(entity instanceof Entity)) return false;
                    // center Vec3 for the BlockPos
                    Vec3 center = Vec3.atCenterOf(blockPos);
                    return entity.isAlive() && entity.distanceToSqr(center) <= (double) Mth.square(cobblemonTrialSpawner.getRequiredPlayerRange());
                });

        List<? extends Entity> list2 = serverLevel.random.nextBoolean() ? entityStream.toList() : playerList;
        if (list2.isEmpty()) {
            return null;
        } else {
            return list2.size() == 1 ? list2.get(0) : Util.getRandom(list2, serverLevel.random);
        }
    }

    private boolean timeToSpawnItemSpawner(ServerLevel arg, CobblemonTrialSpawnerData arg2) {
        return arg.getGameTime() >= arg2.cooldownEndsAt;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= (double)0.0F;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(Level arg, BlockPos arg2, boolean bl) {
        this.particleEmission.emit(arg, arg.getRandom(), arg2, bl);
    }

    public String getSerializedName() {
        return this.name;
    }

    static class LightLevel {
        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private LightLevel() {
        }
    }

    static class SpinningMob {
        private static final double NONE = (double)-1.0F;
        private static final double SLOW = (double)200.0F;
        private static final double FAST = (double)1000.0F;

        private SpinningMob() {
        }
    }

    interface ParticleEmission {
        CobblemonTrialSpawnerState.ParticleEmission NONE = (arg, arg2, arg3, bl) -> {
        };
        CobblemonTrialSpawnerState.ParticleEmission UNOWN = (arg, arg2, arg3, bl) -> {
            if (arg2.nextInt(2) == 0) {
                Vec3 vec3 = arg3.getCenter().offsetRandom(arg2, 0.9F);
                addParticle(ModParticles.UNOWN_PARTICLES.get(), vec3, arg);
            }

        };
        CobblemonTrialSpawnerState.ParticleEmission UNOWN_AND_SPARKS = (arg, arg2, arg3, bl) -> {
            Vec3 vec3 = arg3.getCenter().offsetRandom(arg2, 1.0F);
            addParticle(ModParticles.UNOWN_PARTICLES.get(), vec3, arg);
            addParticle(bl ? ParticleTypes.ELECTRIC_SPARK : ModParticles.UNOWN_PARTICLES.get(), vec3, arg);
        };
        CobblemonTrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (arg, arg2, arg3, bl) -> {
            Vec3 vec3 = arg3.getCenter().offsetRandom(arg2, 0.9F);
            if (arg2.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, vec3, arg);
            }

            if (arg.getGameTime() % 20L == 0L) {
                Vec3 vec32 = arg3.getCenter().add((double)0.0F, (double)0.5F, (double)0.0F);
                int i = arg.getRandom().nextInt(4) + 20;

                for(int j = 0; j < i; ++j) {
                    addParticle(ParticleTypes.SMOKE, vec32, arg);
                }
            }

        };

        private static void addParticle(SimpleParticleType arg, Vec3 arg2, Level arg3) {
            arg3.addParticle(arg, arg2.x(), arg2.y(), arg2.z(), (double)0.0F, (double)0.0F, (double)0.0F);
        }

        void emit(Level arg, RandomSource arg2, BlockPos arg3, boolean bl);
    }
}
