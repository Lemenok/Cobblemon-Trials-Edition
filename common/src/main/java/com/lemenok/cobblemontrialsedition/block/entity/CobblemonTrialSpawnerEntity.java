package com.lemenok.cobblemontrialsedition.block.entity;

import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.*;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class CobblemonTrialSpawnerEntity extends BlockEntity implements CobblemonSpawner, CobblemonTrialSpawner.StateAccessor  {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CobblemonTrialSpawner cobblemonTrialSpawner;

    public CobblemonTrialSpawnerEntity(BlockPos blockPos, BlockState blockState) {
        super(Services.PLATFORM.getCobblemonTrialSpawnerBlockEntity(), blockPos, blockState);
        PlayerDetector playerDetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector entitySelector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        this.cobblemonTrialSpawner = new CobblemonTrialSpawner(CobblemonTrialSpawnerConfig.DEFAULT, CobblemonTrialSpawnerConfig.DEFAULT,
                new CobblemonTrialSpawnerData(), 36000, 14,
                this, playerDetector, entitySelector);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("normal_config")) {
            CompoundTag compoundTag = nbt.getCompound("normal_config").copy();
            nbt.put("ominous_config", compoundTag.merge(nbt.getCompound("ominous_config")));
        }

        DataResult dataResult = this.cobblemonTrialSpawner.codec().parse(NbtOps.INSTANCE, nbt);
        Logger logger = LOGGER;
        Objects.requireNonNull(logger);
        dataResult.resultOrPartial(msg -> LOGGER.error(msg.toString())).ifPresent((object) -> this.cobblemonTrialSpawner = (CobblemonTrialSpawner) object);
        if (this.level != null) {
            this.markUpdated();
        }

    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(nbt, registries);
        this.cobblemonTrialSpawner.codec().encodeStart(NbtOps.INSTANCE, this.cobblemonTrialSpawner).ifSuccess((tag) -> nbt.merge((CompoundTag)tag)).ifError((error) -> LOGGER.warn("Failed to encode TrialSpawner {}", error.message()));
    }

    public void applySpawnerProperties(SpawnerProperties properties, BlockPos blockPos) {
        if (this.level == null) return;

        RegistryAccess registryAccess = this.level.registryAccess();

        // 1. Resolve Loot Tables safely without StructureBlockInfo
        var currentNormalLoot = this.getCobblemonTrialSpawner().getNormalConfig().lootTablesToEject();
        var currentOminousLoot = this.getCobblemonTrialSpawner().getOminousConfig().lootTablesToEject();

        var normalLootTables = properties.lootTables().isEmpty() ?
                currentNormalLoot : properties.lootTables();
        var ominousLootTables = properties.ominousLootTables().isEmpty() ?
                currentOminousLoot : properties.ominousLootTables();

        // 2. Build the Normal Configuration
        CobblemonTrialSpawnerConfig normalConfig = new CobblemonTrialSpawnerConfig(
                properties.spawnRange(),
                properties.totalNumberOfPokemonPerTrial(),
                properties.maximumNumberOfSimultaneousPokemon(),
                properties.totalNumberOfPokemonPerTrialAddedPerPlayer(), // Corrected from your BuildSpawner logic
                properties.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                properties.ticksBetweenSpawnAttempts(),
                properties.ominousSpawnerAttacksEnabled(),
                properties.getListOfPokemonToSpawn(registryAccess, false, blockPos),
                normalLootTables,
                BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
        );

        // 3. Build the Ominous Configuration
        CobblemonTrialSpawnerConfig ominousConfig = new CobblemonTrialSpawnerConfig(
                properties.spawnRange(),
                properties.totalNumberOfPokemonPerTrial(),
                properties.maximumNumberOfSimultaneousPokemon(),
                properties.totalNumberOfPokemonPerTrialAddedPerPlayer(),
                properties.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                properties.ticksBetweenSpawnAttempts(),
                properties.ominousSpawnerAttacksEnabled(),
                properties.getListOfPokemonToSpawn(registryAccess, true, blockPos),
                ominousLootTables,
                BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
        );

        // 4. Apply configurations and properties to the spawner logic
        var spawner = this.getCobblemonTrialSpawner();
        spawner.setConfig(normalConfig, false);
        spawner.setConfig(ominousConfig, true);
        spawner.setTargetCooldownLength(properties.spawnerCooldown());
        spawner.setRequiredPlayerRange(properties.playerDetectionRange());

        var spawnerData = spawner.getData();

        resetSpawnerData(spawnerData, spawner);

        this.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(
                this.getCobblemonTrialSpawner(),
                this.level.random,
                (ServerLevel) this.level
        );

        this.getCobblemonTrialSpawner().markUpdated();
        this.markUpdated();
    }

    public void resetSpawnerData(CobblemonTrialSpawnerData spawnerData, CobblemonTrialSpawner spawner) {
        if (this.level instanceof ServerLevel serverLevel) {
            // Despawn existing Pokemon using their tracked UUIDs
            for (java.util.UUID mobId : spawnerData.getCurrentMobs()) {
                net.minecraft.world.entity.Entity entity = serverLevel.getEntity(mobId);
                if (entity != null) {
                    entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
            }

            // Clear internal data lists (detected players, current mobs, cooldowns)
            spawnerData.reset();

            // Set state back to WAITING_FOR_PLAYERS
            this.setState(serverLevel, CobblemonTrialSpawnerState.WAITING_FOR_PLAYERS);

            // Re-initialize the next spawn data cleanly
            spawnerData.getOrCreateNextSpawnData(
                    spawner,
                    this.level.random,
                    serverLevel
            );
        } else {
            spawnerData.setNextSpawnData(java.util.Optional.empty());
        }
    }

    private SimpleWeightedRandomList<ResourceKey<LootTable>> buildLootTableList(List<ResourceLocation> locations) {
        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> builder = SimpleWeightedRandomList.builder();
        for (ResourceLocation loc : locations) {
            ResourceKey<LootTable> key;
            if (loc.getNamespace().equals(Services.PLATFORM.getModID())) {
                key = ResourceKey.create(Services.PLATFORM.getLootTableRegistry(), loc);
            } else {
                key = ResourceKey.create(Registries.LOOT_TABLE, loc);
            }
            builder.add(key);
        }
        return builder.build();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        return this.cobblemonTrialSpawner.getData().getUpdateTag(this.getBlockState().getValue(CobblemonTrialSpawnerBlock.STATE));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public void setEntityId(EntityType<?> entityType, RandomSource randomSource) {
        if(this.level instanceof ServerLevel serverLevel)
            this.cobblemonTrialSpawner.getData().setEntityId(this.cobblemonTrialSpawner, randomSource, entityType, serverLevel);
        this.setChanged();
    }

    public CobblemonTrialSpawner getCobblemonTrialSpawner() {
        return this.cobblemonTrialSpawner;
    }

    @Override
    public CobblemonTrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(CobblemonTrialSpawnerBlock.STATE) ? CobblemonTrialSpawnerState.INACTIVE : this.getBlockState().getValue(CobblemonTrialSpawnerBlock.STATE);
    }

    @Override
    public void setState(Level level, CobblemonTrialSpawnerState cobblemonTrialSpawnerState) {
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(CobblemonTrialSpawnerBlock.STATE, cobblemonTrialSpawnerState));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
