package com.lemenok.cobblemontrialsedition.block.entity;

import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.models.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class CobblemonTrialSpawnerEntity extends BlockEntity implements CobblemonSpawner, CobblemonTrialSpawner.StateAccessor  {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CobblemonTrialSpawner cobblemonTrialSpawner;

    public CobblemonTrialSpawnerEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), blockPos, blockState);
        PlayerDetector playerDetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector entitySelector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        // TODO: Import config settings here.
        this.cobblemonTrialSpawner = new CobblemonTrialSpawner(CobblemonTrialSpawnerConfig.DEFAULT, CobblemonTrialSpawnerConfig.DEFAULT, new CobblemonTrialSpawnerData(), 1200, 14, this, playerDetector, entitySelector);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("normal_config")) {
            CompoundTag compoundTag = nbt.getCompound("normal_config").copy();
            nbt.put("ominous_config", compoundTag.merge(nbt.getCompound("ominous_config")));
        }

        DataResult var10000 = this.cobblemonTrialSpawner.codec().parse(NbtOps.INSTANCE, nbt);
        Logger var10001 = LOGGER;
        Objects.requireNonNull(var10001);
        var10000.resultOrPartial(msg -> LOGGER.error(msg.toString())).ifPresent((argx) -> this.cobblemonTrialSpawner = (CobblemonTrialSpawner) argx);
        if (this.level != null) {
            this.markUpdated();
        }

    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(nbt, registries);
        this.cobblemonTrialSpawner.codec().encodeStart(NbtOps.INSTANCE, this.cobblemonTrialSpawner).ifSuccess((arg2x) -> nbt.merge((CompoundTag)arg2x)).ifError((error) -> LOGGER.warn("Failed to encode TrialSpawner {}", error.message()));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider arg) {
        return this.cobblemonTrialSpawner.getData().getUpdateTag((CobblemonTrialSpawnerState) this.getBlockState().getValue(CobblemonTrialSpawnerBlock.STATE));
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }


    public void setEntityId(EntityType<?> arg, RandomSource arg2) {
        this.cobblemonTrialSpawner.getData().setEntityId(this.cobblemonTrialSpawner, arg2, arg);
        this.setChanged();
    }

    public CobblemonTrialSpawner getCobblemonTrialSpawner() {
        return this.cobblemonTrialSpawner;
    }

    public void setCobblemonTrialSpawner(CobblemonTrialSpawner cobblemonTrialSpawner) {
        this.cobblemonTrialSpawner = cobblemonTrialSpawner;
        this.setChanged();
    }

    @Override
    public CobblemonTrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(CobblemonTrialSpawnerBlock.STATE) ? CobblemonTrialSpawnerState.INACTIVE : (CobblemonTrialSpawnerState)this.getBlockState().getValue(CobblemonTrialSpawnerBlock.STATE);
    }

    @Override
    public void setState(Level arg, CobblemonTrialSpawnerState arg2) {
        this.setChanged();
        arg.setBlockAndUpdate(this.worldPosition, (BlockState)this.getBlockState().setValue(CobblemonTrialSpawnerBlock.STATE, arg2));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
