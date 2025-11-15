package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.annotations.VisibleForTesting;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.block.entity.trialspawner.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import net.neoforged.neoforge.event.EventHooks;
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
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private CobblemonTrialSpawnerConfig normalConfig;
    private CobblemonTrialSpawnerConfig ominousConfig;
    private CobblemonTrialSpawnerData data;
    private int requiredPlayerRange;
    private int targetCooldownLength;
    private final CobblemonTrialSpawner.StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public Codec<CobblemonTrialSpawner> codec() {
        return RecordCodecBuilder.create((instance) -> instance.group(CobblemonTrialSpawnerConfig.CODEC.optionalFieldOf("normal_config", CobblemonTrialSpawnerConfig.DEFAULT).forGetter(CobblemonTrialSpawner::getNormalConfig), CobblemonTrialSpawnerConfig.CODEC.optionalFieldOf("ominous_config", CobblemonTrialSpawnerConfig.DEFAULT).forGetter(CobblemonTrialSpawner::getOminousConfigForSerialization), CobblemonTrialSpawnerData.MAP_CODEC.forGetter(CobblemonTrialSpawner::getData), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(CobblemonTrialSpawner::getTargetCooldownLength), Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(CobblemonTrialSpawner::getRequiredPlayerRange)).apply(instance, (arg, arg2, arg3, integer, integer2) -> new CobblemonTrialSpawner(arg, arg2, arg3, integer, integer2, this.stateAccessor, this.playerDetector, this.entitySelector)));
    }

    public CobblemonTrialSpawner(CobblemonTrialSpawner.StateAccessor arg, PlayerDetector arg2, PlayerDetector.EntitySelector arg3) {
        this(CobblemonTrialSpawnerConfig.DEFAULT, CobblemonTrialSpawnerConfig.DEFAULT, new CobblemonTrialSpawnerData(), 1200, 14, arg, arg2, arg3);
    }

    public CobblemonTrialSpawner(CobblemonTrialSpawnerConfig arg, CobblemonTrialSpawnerConfig arg2, CobblemonTrialSpawnerData arg3, int i, int j, CobblemonTrialSpawner.StateAccessor arg4, PlayerDetector arg5, PlayerDetector.EntitySelector arg6) {
        this.normalConfig = arg;
        this.ominousConfig = arg2;
        this.data = arg3;
        this.targetCooldownLength = i;
        this.requiredPlayerRange = j;
        this.stateAccessor = arg4;
        this.playerDetector = arg5;
        this.entitySelector = arg6;
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

    public void applyOminous(ServerLevel arg, BlockPos arg2) {
        arg.setBlock(arg2, (BlockState)arg.getBlockState(arg2).setValue(CobblemonTrialSpawnerBlock.OMINOUS, true), 3);
        arg.levelEvent(3020, arg2, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, arg);
    }

    public void removeOminous(ServerLevel arg, BlockPos arg2) {
        arg.setBlock(arg2, (BlockState)arg.getBlockState(arg2).setValue(CobblemonTrialSpawnerBlock.OMINOUS, false), 3);
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
        /*if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        } else {
            return arg.getDifficulty() == Difficulty.PEACEFUL ? false : arg.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        }*/
    }

    public Optional<UUID> spawnMob(ServerLevel arg, BlockPos arg2) {
        RandomSource randomsource = arg.getRandom();
        SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, arg.getRandom());
        CompoundTag compoundtag = spawndata.entityToSpawn();
        ApplyCobblemonRandomModifiers(arg, compoundtag);
        ListTag listtag = compoundtag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundtag);
        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = listtag.size();
            double d0 = i >= 1 ? listtag.getDouble(0) : (double)arg2.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + (double)0.5F;
            double d1 = i >= 2 ? listtag.getDouble(1) : (double)(arg2.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3 ? listtag.getDouble(2) : (double)arg2.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + (double)0.5F;
            if (!arg.noCollision(((EntityType)optional.get()).getSpawnAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3 vec3 = new Vec3(d0, d1, d2);
                if (!inLineOfSight(arg, arg2.getCenter(), vec3)) {
                    return Optional.empty();
                } else {
                    BlockPos blockpos = BlockPos.containing(vec3);
                    if (!SpawnPlacements.checkSpawnRules((EntityType)optional.get(), arg, MobSpawnType.TRIAL_SPAWNER, blockpos, arg.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (spawndata.getCustomSpawnRules().isPresent()) {
                            SpawnData.CustomSpawnRules spawndata$customspawnrules = (SpawnData.CustomSpawnRules)spawndata.getCustomSpawnRules().get();
                            if (!spawndata$customspawnrules.isValidPosition(blockpos, arg)) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityType.loadEntityRecursive(compoundtag, arg, (argx) -> {
                            argx.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return argx;
                        });
                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof Mob) {
                                Mob mob = (Mob)entity;
                                if (!mob.checkSpawnObstruction(arg)) {
                                    return Optional.empty();
                                }

                                boolean flag = spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8);
                                EventHooks.finalizeMobSpawnSpawner(mob, arg, arg.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.TRIAL_SPAWNER, (SpawnGroupData)null, this, flag);
                                mob.setPersistenceRequired();
                                Optional var10000 = spawndata.getEquipment();
                                Objects.requireNonNull(mob);
                                var10000.ifPresent(itemStack -> mob.equip((EquipmentTable) itemStack));
                            }

                            if (!arg.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                UnownParticle unownParticle = this.isOminous ? UnownParticle.OMINOUS : UnownParticle.NORMAL;

                                var encoded = unownParticle.encode();

                                arg.levelEvent(3011, arg2, unownParticle.encode());
                                arg.levelEvent(3012, blockpos, unownParticle.encode());
                                arg.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    private void ApplyCobblemonRandomModifiers(ServerLevel serverLevel, CompoundTag compoundtag) {
        Pokemon pokemon = new Pokemon();
        pokemon.loadFromNBT(serverLevel.registryAccess(), compoundtag);

        CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig = isOminous ? ominousConfig: normalConfig;

    }

    public void ejectReward(ServerLevel arg, BlockPos arg2, ResourceKey<LootTable> arg3) {
        LootTable loottable = arg.getServer().reloadableRegistries().getLootTable(arg3);
        LootParams lootparams = (new LootParams.Builder(arg)).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
        if (!objectarraylist.isEmpty()) {
            ObjectListIterator var7 = objectarraylist.iterator();

            while(var7.hasNext()) {
                ItemStack itemstack = (ItemStack)var7.next();
                DefaultDispenseItemBehavior.spawnItem(arg, itemstack, 2, Direction.UP, Vec3.atBottomCenterOf(arg2).relative(Direction.UP, 1.2));
            }

            arg.levelEvent(3014, arg2, 0);
        }

    }

    public void tickClient(Level arg, BlockPos arg2, boolean bl) {
        CobblemonTrialSpawnerState trialspawnerstate = this.getState();
        trialspawnerstate.emitParticles(arg, arg2, bl);
        if (trialspawnerstate.hasSpinningMob()) {
            double d0 = (double)Math.max(0L, this.data.nextMobSpawnsAt - arg.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + (double)200.0F)) % (double)360.0F;
        }

        if (trialspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = arg.getRandom();
            if (randomsource.nextFloat() <= 0.02F) {
                SoundEvent soundevent = bl ? ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS.get() : ModSounds.COBBLEMON_TRIAL_SPAWNER_AMBIENT.get();
                arg.playLocalSound(arg2, soundevent, SoundSource.BLOCKS, 1f, 1f, false);
            }
        }

    }

    public void tickServer(ServerLevel arg, BlockPos arg2, boolean bl) {
        this.isOminous = bl;
        CobblemonTrialSpawnerState trialspawnerstate = this.getState();
        if (this.data.currentMobs.removeIf((uUID) -> shouldMobBeUntracked(arg, arg2, uUID))) {
            this.data.nextMobSpawnsAt = arg.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
        }

        CobblemonTrialSpawnerState trialspawnerstate1 = trialspawnerstate.tickAndGetNext(arg2, this, arg);
        if (trialspawnerstate1 != trialspawnerstate) {
            this.setState(arg, trialspawnerstate1);
        }

    }

    private static boolean shouldMobBeUntracked(ServerLevel arg, BlockPos arg2, UUID uUID) {
        Entity entity = arg.getEntity(uUID);
        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(arg.dimension()) || entity.blockPosition().distSqr(arg2) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level arg, Vec3 arg2, Vec3 arg3) {
        BlockHitResult blockhitresult = arg.clip(new ClipContext(arg3, arg2, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return blockhitresult.getBlockPos().equals(BlockPos.containing(arg2)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level arg, BlockPos arg2, RandomSource arg3, SimpleParticleType arg4) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)arg2.getX() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            double d1 = (double)arg2.getY() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            double d2 = (double)arg2.getZ() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            arg.addParticle(ParticleTypes.SMOKE, d0, d1, d2, (double)0.0F, (double)0.0F, (double)0.0F);
            arg.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, (double)0.0F, (double)0.0F, (double)0.0F);
        }

    }

    public static void addBecomeOminousParticles(Level arg, BlockPos arg2, RandomSource arg3) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)arg2.getX() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            double d1 = (double)arg2.getY() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            double d2 = (double)arg2.getZ() + (double)0.5F + (arg3.nextDouble() - (double)0.5F) * (double)2.0F;
            double d3 = arg3.nextGaussian() * 0.02;
            double d4 = arg3.nextGaussian() * 0.02;
            double d5 = arg3.nextGaussian() * 0.02;
            //arg.addParticle(ParticleTypes.TRIAL_OMEN, d0, d1, d2, d3, d4, d5);
            arg.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, d3, d4, d5);
        }

    }

    public static void addDetectPlayerParticles(Level arg, BlockPos arg2, RandomSource arg3, int j, ParticleOptions arg4) {
        for(int i = 0; i < 30 + Math.min(j, 10) * 5; ++i) {
            double d0 = (double)(2.0F * arg3.nextFloat() - 1.0F) * 0.65;
            double d1 = (double)(2.0F * arg3.nextFloat() - 1.0F) * 0.65;
            double d2 = (double)arg2.getX() + (double)0.5F + d0;
            double d3 = (double)arg2.getY() + 0.1 + (double)arg3.nextFloat() * 0.8;
            double d4 = (double)arg2.getZ() + (double)0.5F + d1;
            arg.addParticle(arg4, d2, d3, d4, (double)0.0F, (double)0.0F, (double)0.0F);
        }

    }

    public static void addEjectItemParticles(Level arg, BlockPos arg2, RandomSource arg3) {
        for(int i = 0; i < 20; ++i) {
            double d0 = (double)arg2.getX() + 0.4 + arg3.nextDouble() * 0.2;
            double d1 = (double)arg2.getY() + 0.4 + arg3.nextDouble() * 0.2;
            double d2 = (double)arg2.getZ() + 0.4 + arg3.nextDouble() * 0.2;
            double d3 = arg3.nextGaussian() * 0.02;
            double d4 = arg3.nextGaussian() * 0.02;
            double d5 = arg3.nextGaussian() * 0.02;
            arg.addParticle(ModParticles.UNOWN_PARTICLES.get(), d0, d1, d2, d3, d4, d5 * (double)0.25F);
            arg.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
        }

    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector arg) {
        this.playerDetector = arg;
    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public @Nullable Either<BlockEntity, Entity> getOwner() {
        CobblemonTrialSpawner.StateAccessor var2 = this.stateAccessor;
        if (var2 instanceof CobblemonTrialSpawnerEntity be) {
            return Either.left(be);
        } else {
            return null;
        }
    }

    public static enum UnownParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private UnownParticle(SimpleParticleType arg) {
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
        void setState(Level arg, CobblemonTrialSpawnerState arg2);

        CobblemonTrialSpawnerState getState();

        void markUpdated();
    }
}
