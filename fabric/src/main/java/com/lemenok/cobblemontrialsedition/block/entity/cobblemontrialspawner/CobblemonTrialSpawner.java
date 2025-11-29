package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.sound.ModSounds;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CobblemonTrialSpawner implements IOwnedSpawner {
    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(MAX_MOB_TRACKING_DISTANCE);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private CobblemonTrialSpawnerConfig normalConfig;
    private CobblemonTrialSpawnerConfig ominousConfig;
    private CobblemonTrialSpawnerData data;
    private int requiredPlayerRange;
    private int targetCooldownLength;
    private final StateAccessor stateAccessor;
    private final PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean isOminous;

    public Codec<CobblemonTrialSpawner> codec() {
        return RecordCodecBuilder.create((instance) ->
                instance.group(CobblemonTrialSpawnerConfig.CODEC.optionalFieldOf(NORMAL_CONFIG_TAG_NAME,
                        CobblemonTrialSpawnerConfig.DEFAULT).forGetter(CobblemonTrialSpawner::getNormalConfig),
                        CobblemonTrialSpawnerConfig.CODEC
                                .optionalFieldOf(OMINOUS_CONFIG_TAG_NAME, CobblemonTrialSpawnerConfig.DEFAULT)
                                .forGetter(CobblemonTrialSpawner::getOminousConfigForSerialization),
                        CobblemonTrialSpawnerData.MAP_CODEC.forGetter(CobblemonTrialSpawner::getData),
                        Codec.intRange(0, Integer.MAX_VALUE)
                                .optionalFieldOf("target_cooldown_length", DEFAULT_TARGET_COOLDOWN_LENGTH)
                                .forGetter(CobblemonTrialSpawner::getTargetCooldownLength),
                        Codec.intRange(1, 128).optionalFieldOf("required_player_range", DEFAULT_PLAYER_SCAN_RANGE)
                                .forGetter(CobblemonTrialSpawner::getRequiredPlayerRange))
                        .apply(instance, (arg, arg2,
                                          arg3, integer, integer2) ->
                        new CobblemonTrialSpawner(arg, arg2, arg3, integer, integer2, this.stateAccessor, this.playerDetector, this.entitySelector)));
    }

    public CobblemonTrialSpawner(StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this(CobblemonTrialSpawnerConfig.DEFAULT, CobblemonTrialSpawnerConfig.DEFAULT, new CobblemonTrialSpawnerData(), DEFAULT_TARGET_COOLDOWN_LENGTH, DEFAULT_PLAYER_SCAN_RANGE,
                stateAccessor, playerDetector, entitySelector);
    }

    public CobblemonTrialSpawner(CobblemonTrialSpawnerConfig normalConfig, CobblemonTrialSpawnerConfig ominousConfig,
                                 CobblemonTrialSpawnerData cobblemonTrialSpawnerData, int targetCooldownLength,
                                 int requiredPlayerRange, StateAccessor stateAccessor,
                                 PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.data = cobblemonTrialSpawnerData;
        this.targetCooldownLength = targetCooldownLength;
        this.requiredPlayerRange = requiredPlayerRange;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public CobblemonTrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    public void setConfig(CobblemonTrialSpawnerConfig config, boolean isOminous){
        if(isOminous)
            this.ominousConfig = config;
        else
            this.normalConfig = config;
    }

    @VisibleForTesting
    public CobblemonTrialSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    @VisibleForTesting
    public CobblemonTrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private CobblemonTrialSpawnerConfig getOminousConfigForSerialization() {
        return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : CobblemonTrialSpawnerConfig.DEFAULT;
    }

    public void applyOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, (BlockState) serverLevel.getBlockState(blockPos).setValue(CobblemonTrialSpawnerBlock.OMINOUS, true), 3);
        serverLevel.levelEvent(3020, blockPos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, serverLevel);
    }

    public void removeOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, serverLevel.getBlockState(blockPos).setValue(CobblemonTrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public CobblemonTrialSpawnerData getData() {
        return this.data;
    }

    public void setData(CobblemonTrialSpawnerData cobblemonTrialSpawnerData){
        this.data = cobblemonTrialSpawnerData;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public void setTargetCooldownLength(int cooldownLength){
        this.targetCooldownLength = cooldownLength;
    }

    public void setRequiredPlayerRange(int requiredPlayerRange){
        this.requiredPlayerRange = requiredPlayerRange;
    }

    public CobblemonTrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level arg, CobblemonTrialSpawnerState arg2) {
        this.stateAccessor.setState(arg, arg2);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(Level arg) {
        return arg.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
    }

    public Optional<UUID> spawnMob(ServerLevel serverLevel, BlockPos blockPos) {
        RandomSource randomsource = serverLevel.getRandom();
        SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, serverLevel.getRandom());
        CompoundTag compoundtag = spawndata.entityToSpawn();
        ListTag listtag = compoundtag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundtag);
        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = listtag.size();
            double d0 = i >= 1 ? listtag.getDouble(0) : (double) blockPos.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + (double)0.5F;
            double d1 = i >= 2 ? listtag.getDouble(1) : (double)(blockPos.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3 ? listtag.getDouble(2) : (double) blockPos.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + (double)0.5F;
            if (!serverLevel.noCollision(optional.get().getSpawnAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3 vec3 = new Vec3(d0, d1, d2);
                if (!inLineOfSight(serverLevel, blockPos.getCenter(), vec3)) {
                    return Optional.empty();
                } else {
                    BlockPos blockpos = BlockPos.containing(vec3);
                    if (!SpawnPlacements.checkSpawnRules((EntityType)optional.get(), serverLevel, MobSpawnType.TRIAL_SPAWNER, blockpos, serverLevel.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (spawndata.getCustomSpawnRules().isPresent()) {
                            SpawnData.CustomSpawnRules spawndata$customspawnrules = spawndata.getCustomSpawnRules().get();
                            if (!spawndata$customspawnrules.isValidPosition(blockpos, serverLevel)) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityType.loadEntityRecursive(compoundtag, serverLevel, (argx) -> {
                            argx.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return argx;
                        });
                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof Mob) {
                                Mob mob = (Mob)entity;
                                if (!serverLevel.isUnobstructed(mob)) {
                                    return Optional.empty();
                                }

                                boolean flag = spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8);
                                EventHooks.finalizeMobSpawnSpawner(mob, serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.TRIAL_SPAWNER, (SpawnGroupData)null, this, flag);
                                mob.setPersistenceRequired();
                                Optional optionalEquipmentTable = spawndata.getEquipment();
                                Objects.requireNonNull(mob);
                                optionalEquipmentTable.ifPresent(itemStack -> mob.equip((EquipmentTable) itemStack));
                            }

                            if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                UnownParticle unownParticle = this.isOminous ? UnownParticle.OMINOUS : UnownParticle.NORMAL;
                                var encoded = unownParticle.encode();

                                serverLevel.levelEvent(3011, blockPos, unownParticle.encode());
                                serverLevel.levelEvent(3012, blockpos, unownParticle.encode());
                                serverLevel.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<LootTable> lootTableResourceKey) {
        RegistryAccess registryAccess = serverLevel.registryAccess();
        ResourceLocation lootTableResourceLocation = lootTableResourceKey.location();

        LootTable loottable;
        if(lootTableResourceLocation.getNamespace().equals(CobblemonTrialsEditionFabric.MODID)){
            Optional<Holder.Reference<LootTable>> lootTableReference;
            lootTableReference = registryAccess.registryOrThrow(CobblemonTrialsEditionFabric.ClientModEvents.COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY)
                    .getHolder(ResourceKey.create(CobblemonTrialsEditionFabric.ClientModEvents.COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY, lootTableResourceLocation));


            if (lootTableReference.isPresent()) {
                Holder.Reference<LootTable> holder = lootTableReference.get();
                loottable = holder.value();
            } else {
                loottable = LootTable.EMPTY;
            }
        } else
            loottable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableResourceKey);

        LootParams lootparams = (new LootParams.Builder(serverLevel)).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
        if (!objectarraylist.isEmpty()) {
            ObjectListIterator objectListIterator = objectarraylist.iterator();

            while(objectListIterator.hasNext()) {
                ItemStack itemstack = (ItemStack) objectListIterator.next();
                Direction emptySpace = DetermineWhereToSpawnItem(serverLevel, blockPos);
                DefaultDispenseItemBehavior.spawnItem(serverLevel, itemstack, 2, emptySpace, Vec3.atBottomCenterOf(blockPos).relative(emptySpace, 1.2));
            }

            serverLevel.levelEvent(3014, blockPos, 0);
        }

    }

    private Direction DetermineWhereToSpawnItem(ServerLevel level, BlockPos blockPos) {

        // Always dispense upwards first.
        BlockPos posAbove = blockPos.above();
        BlockState stateAbove = level.getBlockState(posAbove);
        if(stateAbove.isAir())
            return Direction.UP;

        // Check each direction for open space to spawn items.
        for (Direction direction : Direction.values()) {

            BlockPos neighborPos = blockPos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.isAir()) {
                return direction;
            }
        }

        return Direction.UP;
    }

    public void tickClient(Level level, BlockPos blockPos, boolean isOminous) {
        CobblemonTrialSpawnerState trialspawnerstate = this.getState();
        trialspawnerstate.emitParticles(level, blockPos, isOminous);
        if (trialspawnerstate.hasSpinningMob()) {
            double d0 = (double)Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + (double)200.0F)) % (double)360.0F;
        }

        if (trialspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = level.getRandom();
            if (randomsource.nextFloat() <= SPAWNING_AMBIENT_SOUND_CHANCE) {
                SoundEvent soundevent = isOminous ? ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS.get() : ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT.get();
                level.playLocalSound(blockPos, soundevent, SoundSource.BLOCKS, 1f, 1f, false);
            }
        }

    }

    public void tickServer(ServerLevel serverLevel, BlockPos blockPos, boolean isOminous) {
        this.isOminous = isOminous;
        CobblemonTrialSpawnerState cobblemonTrialSpawnerState = this.getState();
        if (this.data.currentMobs.removeIf((uUID) -> shouldMobBeUntracked(serverLevel, blockPos, uUID))) {
            this.data.nextMobSpawnsAt = serverLevel.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
        }

        CobblemonTrialSpawnerState cobblemonTrialSpawnerState1 = cobblemonTrialSpawnerState.tickAndGetNext(blockPos, this, serverLevel);
        if (cobblemonTrialSpawnerState1 != cobblemonTrialSpawnerState) {
            this.setState(serverLevel, cobblemonTrialSpawnerState1);
        }

    }

    private static boolean shouldMobBeUntracked(ServerLevel serverLevel, BlockPos blockPos, UUID uUID) {
        Entity entity = serverLevel.getEntity(uUID);
        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(serverLevel.dimension()) || entity.blockPosition().distSqr(blockPos) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 vector3, Vec3 vector32) {
        BlockHitResult blockhitresult = level.clip(new ClipContext(vector32, vector3, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return blockhitresult.getBlockPos().equals(BlockPos.containing(vector3)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double) blockPos.getX() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            double d1 = (double) blockPos.getY() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            double d2 = (double) blockPos.getZ() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0F, 0.0F, 0.0F);
            level.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, 0.0F, 0.0F, 0.0F);
        }
    }

    public static void addBecomeOminousParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double) blockPos.getX() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            double d1 = (double) blockPos.getY() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            double d2 = (double) blockPos.getZ() + (double)0.5F + (randomSource.nextDouble() - (double)0.5F) * (double)2.0F;
            double d3 = randomSource.nextGaussian() * 0.02;
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            level.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, d3, d4, d5);
        }
    }

    public static void addDetectPlayerParticles(Level level, BlockPos blockPos, RandomSource randomSource, int j, ParticleOptions particleOptions) {
        for(int i = 0; i < 30 + Math.min(j, 10) * 5; ++i) {
            double d0 = (double)(2.0F * randomSource.nextFloat() - 1.0F) * 0.65;
            double d1 = (double)(2.0F * randomSource.nextFloat() - 1.0F) * 0.65;
            double d2 = (double) blockPos.getX() + (double)0.5F + d0;
            double d3 = (double) blockPos.getY() + 0.1 + (double) randomSource.nextFloat() * 0.8;
            double d4 = (double) blockPos.getZ() + (double)0.5F + d1;
            level.addParticle(particleOptions, d2, d3, d4, 0.0F, 0.0F, 0.0F);
        }

    }

    public static void addEjectItemParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double) blockPos.getX() + 0.4 + randomSource.nextDouble() * 0.2;
            double d1 = (double) blockPos.getY() + 0.4 + randomSource.nextDouble() * 0.2;
            double d2 = (double) blockPos.getZ() + 0.4 + randomSource.nextDouble() * 0.2;
            double d3 = randomSource.nextGaussian() * 0.02;
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            level.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, d3, d4, d5 * (double)0.25F);
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
        }

    }

    public @Nullable Either<BlockEntity, Entity> getOwner() {
        StateAccessor stateAccessor = this.stateAccessor;
        if (stateAccessor instanceof CobblemonTrialSpawnerEntity be) {
            return Either.left(be);
        } else {
            return null;
        }
    }

    public enum UnownParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        UnownParticle(SimpleParticleType arg) {
            this.particleType = arg;
        }

        public static UnownParticle decode(int i) {
            UnownParticle[] atrialspawner$unownparticle = values();
            return i <= atrialspawner$unownparticle.length && i >= 0 ? atrialspawner$unownparticle[i] : NORMAL;
        }

        public int encode() {
            return this.ordinal();
        }
    }

    public interface StateAccessor {
        void setState(Level level, CobblemonTrialSpawnerState cobblemonTrialSpawnerState);
        CobblemonTrialSpawnerState getState();
        void markUpdated();
    }
}
