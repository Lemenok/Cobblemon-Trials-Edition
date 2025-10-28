package com.lemenok.cobblemontrialsedition.models;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class CobblemonTrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<CobblemonTrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter((arg) -> arg.detectedPlayers), UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter((arg) -> arg.currentMobs), Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter((arg) -> arg.cooldownEndsAt), Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", 0L).forGetter((arg) -> arg.nextMobSpawnsAt), Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter((arg) -> arg.totalMobsSpawned), SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter((arg) -> arg.nextSpawnData), ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter((arg) -> arg.ejectingLootTable)).apply(instance, CobblemonTrialSpawnerData::new));
    protected final Set<UUID> detectedPlayers;
    protected final Set<UUID> currentMobs;
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    @Nullable
    protected ItemStack displayItem;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public CobblemonTrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public CobblemonTrialSpawnerData(Set<UUID> set, Set<UUID> set2, long l, long m, int i, Optional<SpawnData> optional, Optional<ResourceKey<LootTable>> optional2) {
        this.detectedPlayers = new HashSet();
        this.currentMobs = new HashSet();
        this.detectedPlayers.addAll(set);
        this.currentMobs.addAll(set2);
        this.cooldownEndsAt = l;
        this.nextMobSpawnsAt = m;
        this.totalMobsSpawned = i;
        this.nextSpawnData = optional;
        this.ejectingLootTable = optional2;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        /*this.nextSpawnData = Optional.empty();*/
    }

    public boolean hasMobToSpawn(CobblemonTrialSpawner arg, RandomSource arg2) {
        boolean bl = this.getOrCreateNextSpawnData(arg, arg2).getEntityToSpawn().contains("id", 8);
        return bl || !arg.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig arg, int i) {
        return this.totalMobsSpawned >= arg.calculateTargetTotalMobs(i);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel arg, TrialSpawnerConfig arg2, int i) {
        return arg.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < arg2.calculateTargetSimultaneousMobs(i);
    }

    public int countAdditionalPlayers(BlockPos arg) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + String.valueOf(arg) + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel arg, BlockPos arg2, CobblemonTrialSpawner arg3) {
        boolean bl = (arg2.asLong() + arg.getGameTime()) % 20L != 0L;
        if (!bl) {
            if (!arg3.getState().equals(CobblemonTrialSpawnerState.COOLDOWN) || !arg3.isOminous()) {
                List<UUID> list = arg3.getPlayerDetector().detect(arg, arg3.getEntitySelector(), arg2, (double)arg3.getRequiredPlayerRange(), true);
                boolean bl2;
                if (!arg3.isOminous() && !list.isEmpty()) {
                    Optional<Pair<Player, Holder<MobEffect>>> optional = findPlayerWithOminousEffect(arg, list);
                    optional.ifPresent((pair) -> {
                        Player player = (Player)pair.getFirst();
                        if (pair.getSecond() == MobEffects.BAD_OMEN) {
                            transformBadOmenIntoTrialOmen(player);
                        }

                        arg.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                        arg3.applyOminous(arg, arg2);
                    });
                    bl2 = optional.isPresent();
                } else {
                    bl2 = false;
                }

                if (!arg3.getState().equals(CobblemonTrialSpawnerState.COOLDOWN) || bl2) {
                    boolean bl3 = arg3.getData().detectedPlayers.isEmpty();
                    List<UUID> list2 = bl3 ? list : arg3.getPlayerDetector().detect(arg, arg3.getEntitySelector(), arg2, (double)arg3.getRequiredPlayerRange(), false);
                    if (this.detectedPlayers.addAll(list2)) {
                        this.nextMobSpawnsAt = Math.max(arg.getGameTime() + 40L, this.nextMobSpawnsAt);
                        if (!bl2) {
                            int i = arg3.isOminous() ? 3019 : 3013;
                            arg.levelEvent(i, arg2, this.detectedPlayers.size());
                        }
                    }

                }
            }
        }
    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel arg, List<UUID> list) {
        Player player = null;

        for(UUID uUID : list) {
            Player player2 = arg.getPlayerByUUID(uUID);
            if (player2 != null) {
                Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
                if (player2.hasEffect(holder)) {
                    return Optional.of(Pair.of(player2, holder));
                }

                if (player2.hasEffect(MobEffects.BAD_OMEN)) {
                    player = player2;
                }
            }
        }

        return Optional.ofNullable(player).map((argx) -> Pair.of(argx, MobEffects.BAD_OMEN));
    }

    public void resetAfterBecomingOminous(CobblemonTrialSpawner cobblemonTrialSpawner, ServerLevel serverLevel) {
        Stream var10000 = this.currentMobs.stream();
        Objects.requireNonNull(serverLevel);
        var10000.map(id -> serverLevel.getEntity((UUID) id)).forEach((arg2x) -> {
            if (arg2x != null) {
                if (arg2x instanceof Entity entity) {
                    serverLevel.levelEvent(3012, entity.blockPosition(), CobblemonTrialSpawner.FlameParticle.NORMAL.encode());
                    if (arg2x instanceof Mob) {
                        Mob mob = (Mob)arg2x;
                        mob.dropPreservedEquipment();
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }

            }
        });
        if (!cobblemonTrialSpawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = serverLevel.getGameTime() + (long) cobblemonTrialSpawner.getOminousConfig().ticksBetweenSpawn();
        cobblemonTrialSpawner.markUpdated();
        this.cooldownEndsAt = serverLevel.getGameTime() + cobblemonTrialSpawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoTrialOmen(Player arg) {
        MobEffectInstance mobEffectInstance = arg.getEffect(MobEffects.BAD_OMEN);
        if (mobEffectInstance != null) {
            int i = mobEffectInstance.getAmplifier() + 1;
            int j = 18000 * i;
            arg.removeEffect(MobEffects.BAD_OMEN);
            arg.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
        }
    }

    public boolean isReadyToOpenShutter(ServerLevel arg, float f, int i) {
        long l = this.cooldownEndsAt - (long)i;
        return (float)arg.getGameTime() >= (float)l + f;
    }

    public boolean isReadyToEjectItems(ServerLevel arg, float f, int i) {
        long l = this.cooldownEndsAt - (long)i;
        return (float)(arg.getGameTime() - l) % f == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel arg) {
        return arg.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(CobblemonTrialSpawner arg, RandomSource arg2, EntityType<?> arg3) {
        this.getOrCreateNextSpawnData(arg, arg2).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(arg3).toString());
    }

    public SpawnData getOrCreateNextSpawnData(CobblemonTrialSpawner arg, RandomSource arg2) {
        if (this.nextSpawnData.isPresent()) {
            //ApplySpawnVariance
            return (SpawnData)this.nextSpawnData.get();
        } else {
            SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList = arg.getConfig().spawnPotentialsDefinition();
            Optional<SpawnData> optional = simpleWeightedRandomList.isEmpty() ? this.nextSpawnData : simpleWeightedRandomList.getRandom(arg2).map(WeightedEntry.Wrapper::data);
            this.nextSpawnData = Optional.of((SpawnData)optional.orElseGet(SpawnData::new));
            arg.markUpdated();
            return (SpawnData)this.nextSpawnData.get();
        }
    }

    @Nullable
    public ItemStack getOrCreateDisplayEntity(boolean isOminous, Level arg2, CobblemonTrialSpawnerState arg3) {
        if (!arg3.hasSpinningMob()) {
            return null;
        } else {
            if (this.displayItem == null) {
                //CompoundTag compoundTag = this.getOrCreateNextSpawnData(arg, arg2.getRandom()).getEntityToSpawn();
                //if (compoundTag.contains("id", 8)) {
                    //this.displayItem = EntityType.loadEntityRecursive(compoundTag, arg2, Function.identity());
                if (isOminous)
                    this.displayItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon","master_ball")).getDefaultInstance();
                else
                    this.displayItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon","poke_ball")).getDefaultInstance();
            }

            return this.displayItem;
        }
    }

    public CompoundTag getUpdateTag(CobblemonTrialSpawnerState arg) {
        CompoundTag compoundTag = new CompoundTag();
        if (arg == CobblemonTrialSpawnerState.ACTIVE) {
            compoundTag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData.ifPresent((arg2) -> compoundTag.put("spawn_data", (Tag)SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, arg2).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))));
        return compoundTag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel arg, TrialSpawnerConfig arg2, BlockPos arg3) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable lootTable = arg.getServer().reloadableRegistries().getLootTable(arg2.itemsToDropWhenOminous());
            LootParams lootParams = (new LootParams.Builder(arg)).create(LootContextParamSets.EMPTY);
            long l = lowResolutionPosition(arg, arg3);
            ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams, l);
            if (objectArrayList.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder();
                ObjectListIterator var10 = objectArrayList.iterator();

                while(var10.hasNext()) {
                    ItemStack itemStack = (ItemStack)var10.next();
                    builder.add(itemStack.copyWithCount(1), itemStack.getCount());
                }

                this.dispensing = builder.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(ServerLevel arg, BlockPos arg2) {
        BlockPos blockPos = new BlockPos(Mth.floor((float)arg2.getX() / 30.0F), Mth.floor((float)arg2.getY() / 20.0F), Mth.floor((float)arg2.getZ() / 30.0F));
        return arg.getSeed() + blockPos.asLong();
    }

    public Set<UUID> getCurrentMobs() {
        return currentMobs;
    }
}
