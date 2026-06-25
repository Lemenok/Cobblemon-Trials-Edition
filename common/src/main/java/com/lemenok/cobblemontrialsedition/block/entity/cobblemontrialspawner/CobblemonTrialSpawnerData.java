package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.google.common.collect.Sets;
import com.lemenok.cobblemontrialsedition.platform.Services;
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
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.*;
import java.util.stream.Stream;

public class CobblemonTrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<CobblemonTrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter((arg) -> arg.detectedPlayers),
                    UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter((arg) -> arg.currentMobs),
                    Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", 0L).forGetter((arg) -> arg.cooldownEndsAt),
                    Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", 0L).forGetter((arg) -> arg.nextMobSpawnsAt),
                    Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter((arg) -> arg.totalMobsSpawned),
                    SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter((arg) -> arg.nextSpawnData),
                    ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter((arg) -> arg.ejectingLootTable)).apply(instance, CobblemonTrialSpawnerData::new));
    protected final Set<UUID> detectedPlayers;
    protected final Set<UUID> currentMobs;
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    protected ItemStack displayItem;
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public CobblemonTrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public CobblemonTrialSpawnerData(Set<UUID> setOfDetectedPlayers, Set<UUID> setOfCurrentMobs, long cooldownEndsAt,
                                     long nextMobSpawnsAt, int totalMobsSpawned, Optional<SpawnData> nextSpawnData,
                                     Optional<ResourceKey<LootTable>> ejectingLootTable) {
        this.detectedPlayers = new HashSet();
        this.currentMobs = new HashSet();
        this.detectedPlayers.addAll(setOfDetectedPlayers);
        this.currentMobs.addAll(setOfCurrentMobs);
        this.cooldownEndsAt = cooldownEndsAt;
        this.nextMobSpawnsAt = nextMobSpawnsAt;
        this.totalMobsSpawned = totalMobsSpawned;
        this.nextSpawnData = nextSpawnData;
        this.ejectingLootTable = ejectingLootTable;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(CobblemonTrialSpawner cobblemonTrialSpawner, RandomSource randomSource, ServerLevel serverLevel) {
        boolean isEntityAvailableForSpawn = this.getOrCreateNextSpawnData(cobblemonTrialSpawner, randomSource, serverLevel).getEntityToSpawn().contains("id", 8);
        return isEntityAvailableForSpawn || !cobblemonTrialSpawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig, int i) {
        return this.totalMobsSpawned >= cobblemonTrialSpawnerConfig.calculateTargetTotalMobs(i);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel serverLevel, CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig, int i) {
        return serverLevel.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < cobblemonTrialSpawnerConfig.calculateTargetSimultaneousMobs(i);
    }

    public int countAdditionalPlayers(BlockPos blockPos) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Cobblemon Trial Spawner at " + blockPos + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel serverLevel, BlockPos blockPos, CobblemonTrialSpawner cobblemonTrialSpawner) {
        boolean bl = (blockPos.asLong() + serverLevel.getGameTime()) % 20L != 0L;
        if (!bl) {
            if (!cobblemonTrialSpawner.getState().equals(CobblemonTrialSpawnerState.COOLDOWN) || !cobblemonTrialSpawner.isOminous()) {
                List<UUID> list = cobblemonTrialSpawner.getPlayerDetector().detect(serverLevel, cobblemonTrialSpawner.getEntitySelector(), blockPos, (double) cobblemonTrialSpawner.getRequiredPlayerRange(), true);
                boolean bl2;
                if (!cobblemonTrialSpawner.isOminous() && !list.isEmpty()) {
                    Optional<Pair<Player, Holder<MobEffect>>> optional = findPlayerWithOminousEffect(serverLevel, list);
                    optional.ifPresent((pair) -> {
                        Player player = (Player)pair.getFirst();
                        if (pair.getSecond() == MobEffects.BAD_OMEN) {
                            transformBadOmenIntoTrialOmen(player);
                        }

                        serverLevel.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                        cobblemonTrialSpawner.applyOminous(serverLevel, blockPos);
                    });
                    bl2 = optional.isPresent();
                } else {
                    bl2 = false;
                }

                if (!cobblemonTrialSpawner.getState().equals(CobblemonTrialSpawnerState.COOLDOWN) || bl2) {
                    boolean bl3 = cobblemonTrialSpawner.getData().detectedPlayers.isEmpty();
                    List<UUID> list2 = bl3 ? list : cobblemonTrialSpawner.getPlayerDetector().detect(serverLevel, cobblemonTrialSpawner.getEntitySelector(), blockPos, (double) cobblemonTrialSpawner.getRequiredPlayerRange(), false);
                    if (this.detectedPlayers.addAll(list2)) {
                        this.nextMobSpawnsAt = Math.max(serverLevel.getGameTime() + 40L, this.nextMobSpawnsAt);
                        if (!bl2) {
                            int i = cobblemonTrialSpawner.isOminous() ? 3019 : 3013;
                            serverLevel.levelEvent(i, blockPos, this.detectedPlayers.size());
                        }
                    }

                }
            }
        }
    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel serverLevel, List<UUID> listOfPlayers) {
        Player player = null;

        for(UUID uUID : listOfPlayers) {
            Player player2 = serverLevel.getPlayerByUUID(uUID);
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

        return Optional.ofNullable(player).map((playerarg) -> Pair.of(playerarg, MobEffects.BAD_OMEN));
    }

    public void resetAfterBecomingOminous(CobblemonTrialSpawner cobblemonTrialSpawner, ServerLevel serverLevel) {
        Stream stream = this.currentMobs.stream();
        Objects.requireNonNull(serverLevel);
        stream.map(id -> serverLevel.getEntity((UUID) id)).forEach((arg2x) -> {
            if (arg2x != null) {
                if (arg2x instanceof Entity entity) {
                    serverLevel.levelEvent(3012, entity.blockPosition(), CobblemonTrialSpawner.UnownParticle.NORMAL.encode());
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

    public boolean isReadyToOpenShutter(ServerLevel serverLevel, float shutterDelay, int cooldownLength) {
        long l = this.cooldownEndsAt - (long)cooldownLength;
        return (float) serverLevel.getGameTime() >= (float)l + shutterDelay;
    }

    public boolean isReadyToEjectItems(ServerLevel serverLevel, float f, int i) {
        long l = this.cooldownEndsAt - (long)i;
        return (float)(serverLevel.getGameTime() - l) % f == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel serverLevel) {
        return serverLevel.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(CobblemonTrialSpawner cobblemonTrialSpawner, RandomSource randomSource, EntityType<?> entityType, ServerLevel serverLevel) {
        this.getOrCreateNextSpawnData(cobblemonTrialSpawner, randomSource, serverLevel).getEntityToSpawn()
                .putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    public SpawnData getOrCreateNextSpawnData(CobblemonTrialSpawner cobblemonTrialSpawner, RandomSource randomSource, ServerLevel level) {
        if (this.nextSpawnData.isPresent()) {
            if(Services.PLATFORM.getCommonConfig().ENABLE_POKEMON_LEVEL_ADJUSTMENT){
                return CalculateLevelFromNearbyPlayers(this.nextSpawnData, level, cobblemonTrialSpawner).get();
            }
            else {
                return this.nextSpawnData.get();
            }

        } else {
            SimpleWeightedRandomList<SpawnData> simpleWeightedRandomList = cobblemonTrialSpawner.getConfig().spawnPotentialsDefinition();
            Optional<SpawnData> optional = simpleWeightedRandomList.isEmpty() ? this.nextSpawnData : simpleWeightedRandomList.getRandom(randomSource).map(WeightedEntry.Wrapper::data);

            // Check if we should apply LeveL adjustment based on nearby players.
            if(Services.PLATFORM.getCommonConfig().ENABLE_POKEMON_LEVEL_ADJUSTMENT){
                this.nextSpawnData = CalculateLevelFromNearbyPlayers(Optional.of(optional.orElseGet(SpawnData::new)), level, cobblemonTrialSpawner);
            }
            else {
                this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
            }

            cobblemonTrialSpawner.markUpdated();
            return this.nextSpawnData.get();
        }
    }

    private Optional<SpawnData> CalculateLevelFromNearbyPlayers(Optional<SpawnData> nextSpawnData, ServerLevel serverLevel, CobblemonTrialSpawner cobblemonTrialSpawner) {
        if(serverLevel == null){
            return this.nextSpawnData;
        }

        List<Integer> listOfLevels = new ArrayList<>();
        var detectedPlayers = cobblemonTrialSpawner.getData().detectedPlayers;

        for(UUID uuid : detectedPlayers) {
            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(uuid);

            if(serverPlayer == null){
                return this.nextSpawnData;
            }

            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);

            party.spliterator().forEachRemaining(pokemon -> {
                listOfLevels.add(pokemon.getLevel());
            });
        }

        // Active Players have no Pokemon in party.
        if(listOfLevels.isEmpty())
            return nextSpawnData;

        int calculatedLevel;

        // Calculate Median among all player Pokemon
        if(Objects.equals(Services.PLATFORM.getCommonConfig().POKEMON_LEVEL_ADJUSTMENT_TYPE, "MEDIAN")) calculatedLevel = CalculateMedian(listOfLevels);
            // Calculate Average among all player Pokemon
        else if (Objects.equals(Services.PLATFORM.getCommonConfig().POKEMON_LEVEL_ADJUSTMENT_TYPE, "AVERAGE")) calculatedLevel = CalculateAverage(listOfLevels);
            // Other Values default to Average.
        else calculatedLevel = CalculateAverage(listOfLevels);

        // Update spawn data with calculated Pokemon level.
        nextSpawnData.ifPresent(spawnData -> {
            CompoundTag nbt = spawnData.entityToSpawn();
            CompoundTag pokemonTag = nbt.getCompound("Pokemon");
            pokemonTag.putInt("Level", calculatedLevel);
            nbt.put("Pokemon", pokemonTag);
        });

        return nextSpawnData;
    }

    private int CalculateMedian(List<Integer> listOfLevels) {
        int size = listOfLevels.size();
        if (size % 2 != 0) {
            // If Odd number of elements the middle element
            return listOfLevels.get(size / 2);
        } else {
            // If Even number of elements the average of the two middle elements
            return (int) ((listOfLevels.get(size / 2 - 1) + listOfLevels.get(size / 2)) / 2.0);
        }
    }

    private int CalculateAverage(List<Integer> listOfLevels) {
        int sum = 0;

        for(int level : listOfLevels){
            sum += level;
        }

        return sum / listOfLevels.size();
    }

    public ItemStack getOrCreateDisplayEntity(boolean isOminous, CobblemonTrialSpawner cobblemonTrialSpawner,
                                              Level level, CobblemonTrialSpawnerState cobblemonTrialSpawnerState) {
        if (isOminous)
            return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon","ancient_gigaton_ball")).getDefaultInstance();
        else
            return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("cobblemon","ancient_slate_ball")).getDefaultInstance();
    }

    public CompoundTag getUpdateTag(CobblemonTrialSpawnerState cobblemonTrialSpawnerState) {
        CompoundTag compoundTag = new CompoundTag();
        if (cobblemonTrialSpawnerState == CobblemonTrialSpawnerState.ACTIVE) {
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

    public SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel serverLevel,
                                      CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig, BlockPos blockPos) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(cobblemonTrialSpawnerConfig.itemsToDropWhenOminous());
            LootParams lootParams = (new LootParams.Builder(serverLevel)).create(LootContextParamSets.EMPTY);
            long position = lowResolutionPosition(serverLevel, blockPos);
            ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams, position);
            if (objectArrayList.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder();
                ObjectListIterator iterator = objectArrayList.iterator();

                while(iterator.hasNext()) {
                    ItemStack itemStack = (ItemStack) iterator.next();
                    builder.add(itemStack.copyWithCount(1), itemStack.getCount());
                }

                this.dispensing = builder.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos lowResolutionBlockPos = new BlockPos(Mth.floor((float) blockPos.getX() / 30.0F), Mth.floor((float) blockPos.getY() / 20.0F), Mth.floor((float) blockPos.getZ() / 30.0F));
        return serverLevel.getSeed() + lowResolutionBlockPos.asLong();
    }

    public Set<UUID> getCurrentMobs() {
        return currentMobs;
    }
}
