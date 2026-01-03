package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonSpawner;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CobblemonTrialSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<CobblemonTrialSpawnerBlock> CODEC = simpleCodec(CobblemonTrialSpawnerBlock::new);
    public static final EnumProperty<CobblemonTrialSpawnerState> STATE;
    public static final BooleanProperty OMINOUS;

    @Override
    public @NotNull MapCodec<CobblemonTrialSpawnerBlock> codec() { return CODEC; }

    public CobblemonTrialSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STATE, CobblemonTrialSpawnerState.INACTIVE).setValue(OMINOUS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> arg) {
        arg.add(new Property[]{STATE, OMINOUS});
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }


    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) { return new CobblemonTrialSpawnerEntity(blockPos, blockState); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState blockState, BlockEntityType<T> blockEntityType) {
        BlockEntityTicker blockEntityTicker;
        if (level instanceof ServerLevel serverLevel) {
            blockEntityTicker = createTickerHelper(blockEntityType, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(),
                    (level1, blockPos, blockState1, cobblemonTrialSpawnerEntity) ->
                            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().tickServer(serverLevel, blockPos,
                                    blockState1.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false)));
        } else {
            blockEntityTicker = createTickerHelper(blockEntityType, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(),
                    (level2, blockPos, blockState1, cobblemonTrialSpawnerEntity) ->
                            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().tickClient(level2, blockPos,
                                    blockState1.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false)));
        }

        return blockEntityTicker;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.@NotNull TooltipContext tooltipContext,
                                @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
        CobblemonSpawner.appendHoverText(itemStack, list, "spawn_data");
    }

    static {
        STATE = EnumProperty.create("cobblemon_trial_spawner_state", CobblemonTrialSpawnerState.class);
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}
