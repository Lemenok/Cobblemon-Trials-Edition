package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public enum CobblemonTrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, CobblemonTrialSpawnerState.ParticleEmission.NONE, -1.0F, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmission.UNOWN, 200.0F, true),
    ACTIVE("active", 8, ParticleEmission.UNOWN_AND_SPARKS, 1000.0F, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, CobblemonTrialSpawnerState.ParticleEmission.UNOWN, -1.0F, false),
    EJECTING_REWARD("ejecting_reward", 8, CobblemonTrialSpawnerState.ParticleEmission.UNOWN, -1.0F, false),
    COOLDOWN("cooldown", 0, CobblemonTrialSpawnerState.ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0F, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final CobblemonTrialSpawnerState.ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private CobblemonTrialSpawnerState(String state, int lightLevel, CobblemonTrialSpawnerState.ParticleEmission particleEmission, double spinningMobSpeed, boolean isCapableOfSpawning) {
        this.name = state;
        this.lightLevel = lightLevel;
        this.particleEmission = particleEmission;
        this.spinningMobSpeed = spinningMobSpeed;
        this.isCapableOfSpawning = isCapableOfSpawning;
    }

    CobblemonTrialSpawnerState tickAndGetNext(BlockPos blockPos, CobblemonTrialSpawner cobblemonTrialSpawner, ServerLevel serverLevel) {
        CobblemonTrialSpawnerData cobblemonTrialSpawnerData = cobblemonTrialSpawner.getData();
        CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig = cobblemonTrialSpawner.getConfig();
        CobblemonTrialSpawnerState cobblemonTrialSpawnerState;
        switch (this.ordinal()) {
            case 0:
                cobblemonTrialSpawnerState = cobblemonTrialSpawnerData.getOrCreateDisplayEntity(false, cobblemonTrialSpawner, serverLevel, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
                break;
            case 1:
                if (!cobblemonTrialSpawner.canSpawnInLevel(serverLevel)) {
                    cobblemonTrialSpawnerData.reset();
                    cobblemonTrialSpawnerState = this;
                } else if (!cobblemonTrialSpawnerData.hasMobToSpawn(cobblemonTrialSpawner, serverLevel.random)) {
                    cobblemonTrialSpawnerState = INACTIVE;
                } else {
                    cobblemonTrialSpawnerData.tryDetectPlayers(serverLevel, blockPos, cobblemonTrialSpawner);
                    cobblemonTrialSpawnerState = cobblemonTrialSpawnerData.detectedPlayers.isEmpty() ? this : ACTIVE;
                }
                break;
            case 2:
                if (!cobblemonTrialSpawner.canSpawnInLevel(serverLevel)) {
                    cobblemonTrialSpawnerData.reset();
                    cobblemonTrialSpawnerState = WAITING_FOR_PLAYERS;
                } else if (!cobblemonTrialSpawnerData.hasMobToSpawn(cobblemonTrialSpawner, serverLevel.random)) {
                    cobblemonTrialSpawnerState = INACTIVE;
                } else {
                    int i = cobblemonTrialSpawnerData.countAdditionalPlayers(blockPos);
                    cobblemonTrialSpawnerData.tryDetectPlayers(serverLevel, blockPos, cobblemonTrialSpawner);
                    if (cobblemonTrialSpawner.isOminous() && cobblemonTrialSpawnerConfig.enableOminousSpawnerAttacks()) {
                        this.spawnOminousOminousItemSpawner(serverLevel, blockPos, cobblemonTrialSpawner);
                    }

                    if (cobblemonTrialSpawnerData.hasFinishedSpawningAllMobs(cobblemonTrialSpawnerConfig, i)) {
                        if (cobblemonTrialSpawnerData.haveAllCurrentMobsDied()) {
                            cobblemonTrialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + (long) cobblemonTrialSpawner.getTargetCooldownLength();
                            cobblemonTrialSpawnerData.totalMobsSpawned = 0;
                            cobblemonTrialSpawnerData.nextMobSpawnsAt = 0L;
                            cobblemonTrialSpawnerState = WAITING_FOR_REWARD_EJECTION;
                            break;
                        }
                    } else if (cobblemonTrialSpawnerData.isReadyToSpawnNextMob(serverLevel, cobblemonTrialSpawnerConfig, i)) {
                        cobblemonTrialSpawner.spawnMob(serverLevel, blockPos).ifPresent((uUID) -> {
                            cobblemonTrialSpawnerData.currentMobs.add(uUID);
                            ++cobblemonTrialSpawnerData.totalMobsSpawned;
                            cobblemonTrialSpawnerData.nextMobSpawnsAt = serverLevel.getGameTime() + (long) cobblemonTrialSpawnerConfig.ticksBetweenSpawn();
                            cobblemonTrialSpawnerConfig.spawnPotentialsDefinition().getRandom(serverLevel.getRandom()).ifPresent((arg3x) -> {
                                cobblemonTrialSpawnerData.nextSpawnData = Optional.of((SpawnData)arg3x.data());
                                cobblemonTrialSpawner.markUpdated();
                            });
                        });
                    }

                    cobblemonTrialSpawnerState = this;
                }
                break;
            case 3:
                if (cobblemonTrialSpawnerData.isReadyToOpenShutter(serverLevel, DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB, cobblemonTrialSpawner.getTargetCooldownLength())) {
                    serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS);
                    cobblemonTrialSpawnerState = EJECTING_REWARD;
                } else {
                    cobblemonTrialSpawnerState = this;
                }
                break;
            case 4:
                if (!cobblemonTrialSpawnerData.isReadyToEjectItems(serverLevel, (float)TIME_BETWEEN_EACH_EJECTION, cobblemonTrialSpawner.getTargetCooldownLength())) {
                    cobblemonTrialSpawnerState = this;
                } else if (cobblemonTrialSpawnerData.detectedPlayers.isEmpty()) {
                    serverLevel.playSound(null, blockPos, SoundEvents.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS);
                    cobblemonTrialSpawnerData.ejectingLootTable = Optional.empty();
                    cobblemonTrialSpawnerState = COOLDOWN;
                } else {
                    if (cobblemonTrialSpawnerData.ejectingLootTable.isEmpty()) {
                        cobblemonTrialSpawnerData.ejectingLootTable = cobblemonTrialSpawnerConfig.lootTablesToEject().getRandomValue(serverLevel.getRandom());
                    }

                    cobblemonTrialSpawnerData.ejectingLootTable.ifPresent((arg4) -> cobblemonTrialSpawner.ejectReward(serverLevel, blockPos, arg4));
                    cobblemonTrialSpawnerData.detectedPlayers.remove(cobblemonTrialSpawnerData.detectedPlayers.iterator().next());
                    cobblemonTrialSpawnerState = this;
                }
                break;
            case 5:
                cobblemonTrialSpawnerData.tryDetectPlayers(serverLevel, blockPos, cobblemonTrialSpawner);
                if (!cobblemonTrialSpawnerData.detectedPlayers.isEmpty()) {
                    cobblemonTrialSpawnerData.totalMobsSpawned = 0;
                    cobblemonTrialSpawnerData.nextMobSpawnsAt = 0L;
                    cobblemonTrialSpawnerState = ACTIVE;
                } else if (cobblemonTrialSpawnerData.isCooldownFinished(serverLevel)) {
                    cobblemonTrialSpawner.removeOminous(serverLevel, blockPos);
                    cobblemonTrialSpawnerData.reset();
                    cobblemonTrialSpawnerState = WAITING_FOR_PLAYERS;
                } else {
                    cobblemonTrialSpawnerState = this;
                }
                break;
            default:
                throw new MatchException(null, null);
        }

        return cobblemonTrialSpawnerState;
    }

    private void spawnOminousOminousItemSpawner(ServerLevel serverLevel, BlockPos blockPos, CobblemonTrialSpawner cobblemonTrialSpawner) {
        CobblemonTrialSpawnerData trialSpawnerData = cobblemonTrialSpawner.getData();
        CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig = cobblemonTrialSpawner.getConfig();
        ItemStack itemStack = trialSpawnerData.getDispensingItems(serverLevel, cobblemonTrialSpawnerConfig, blockPos).getRandomValue(serverLevel.random).orElse(ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            if (this.timeToSpawnItemSpawner(serverLevel, trialSpawnerData)) {
                calculatePositionToSpawnSpawner(serverLevel, blockPos, cobblemonTrialSpawner, trialSpawnerData).ifPresent((arg5) -> {
                    OminousItemSpawner ominousItemSpawner = OminousItemSpawner.create(serverLevel, itemStack);
                    ominousItemSpawner.moveTo(arg5);
                    serverLevel.addFreshEntity(ominousItemSpawner);
                    float f = (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2F + 1.0F;
                    serverLevel.playSound(null, BlockPos.containing(arg5), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, f);
                    trialSpawnerData.cooldownEndsAt = serverLevel.getGameTime() + cobblemonTrialSpawner.getOminousConfig().ticksBetweenItemSpawners();
                });
            }

        }
    }

    private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel serverLevel, BlockPos blockPosition,
                  CobblemonTrialSpawner cobblemonTrialSpawner, CobblemonTrialSpawnerData cobblemonTrialSpawnerData) {
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

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel serverLevel) {
        Vec3 vec3 = entity.position();
        Vec3 vec32 = vec3.relative(Direction.UP, entity.getBbHeight() + 2.0F + (float) serverLevel.random.nextInt(4));
        BlockHitResult blockHitResult = serverLevel.clip(new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        Vec3 vec33 = blockHitResult.getBlockPos().getCenter().relative(Direction.DOWN, 1.0F);
        BlockPos blockPos = BlockPos.containing(vec33);
        return !serverLevel.getBlockState(blockPos).getCollisionShape(serverLevel, blockPos).isEmpty() ? Optional.empty() : Optional.of(vec33);
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

    private boolean timeToSpawnItemSpawner(ServerLevel serverLevel, CobblemonTrialSpawnerData cobblemonTrialSpawnerData) {
        return serverLevel.getGameTime() >= cobblemonTrialSpawnerData.cooldownEndsAt;
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

    public void emitParticles(Level level, BlockPos blockPos, boolean bl) {
        this.particleEmission.emit(level, level.getRandom(), blockPos, bl);
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
        private static final double NONE = -1.0F;
        private static final double SLOW = 200.0F;
        private static final double FAST = 1000.0F;

        private SpinningMob() {
        }
    }

    interface ParticleEmission {
        CobblemonTrialSpawnerState.ParticleEmission NONE = (level, randomSource, blockPos, isCapableOfSpawning) -> {
        };
        CobblemonTrialSpawnerState.ParticleEmission UNOWN = (level, randomSource, blockPos, isCapableOfSpawning) -> {
            if (randomSource.nextInt(2) == 0) {
                Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9F);
                addParticle(ModParticles.UNOWN_PARTICLES.get(), vec3, level);
            }

        };
        CobblemonTrialSpawnerState.ParticleEmission UNOWN_AND_SPARKS = (level, randomSource, blockPos, isCapableOfSpawning) -> {
            Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 1.0F);
            addParticle(ModParticles.UNOWN_PARTICLES.get(), vec3, level);
            addParticle(isCapableOfSpawning ? ParticleTypes.ELECTRIC_SPARK : ModParticles.UNOWN_PARTICLES.get(), vec3, level);
        };
        CobblemonTrialSpawnerState.ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (level, randomSource, blockPos, isCapableOfSpawning) -> {
            Vec3 vec3 = blockPos.getCenter().offsetRandom(randomSource, 0.9F);
            if (randomSource.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, vec3, level);
            }

            if (level.getGameTime() % 20L == 0L) {
                Vec3 vec32 = blockPos.getCenter().add(0.0F, 0.5F, 0.0F);
                int i = level.getRandom().nextInt(4) + 20;

                for(int j = 0; j < i; ++j) {
                    addParticle(ParticleTypes.SMOKE, vec32, level);
                }
            }

        };

        private static void addParticle(SimpleParticleType simpleParticleType, Vec3 vec3, Level level) {
            level.addParticle(simpleParticleType, vec3.x(), vec3.y(), vec3.z(), 0.0F, 0.0F, 0.0F);
        }

        void emit(Level level, RandomSource randomSource, BlockPos blockPos, boolean bl);
    }
}
