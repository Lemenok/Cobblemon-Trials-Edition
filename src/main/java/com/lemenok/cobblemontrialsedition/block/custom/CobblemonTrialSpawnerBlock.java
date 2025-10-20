package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CobblemonTrialSpawnerBlock extends TrialSpawnerBlock {
    public static final MapCodec<TrialSpawnerBlock> CODEC = simpleCodec(CobblemonTrialSpawnerBlock::new);

    public CobblemonTrialSpawnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TrialSpawnerBlock> codec() {
        return CODEC;
    }

    /* BLOCK ENTITY */

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), CobblemonTrialSpawnerEntity::tick);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CobblemonTrialSpawnerEntity(blockPos, blockState);
    }
}
