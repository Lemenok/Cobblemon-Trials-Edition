package com.lemenok.cobblemontrialsedition.block.entity;

import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

public class CobblemonTrialSpawnerEntity extends BlockEntity implements CobblemonSpawner, CobblemonTrialSpawner.StateAccessor  {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CobblemonTrialSpawner cobblemonTrialSpawner;

    public CobblemonTrialSpawnerEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.COBBLEMON_TRIAL_SPAWNER, blockPos, blockState);
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
        this.cobblemonTrialSpawner.getData().setEntityId(this.cobblemonTrialSpawner, randomSource, entityType);
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
