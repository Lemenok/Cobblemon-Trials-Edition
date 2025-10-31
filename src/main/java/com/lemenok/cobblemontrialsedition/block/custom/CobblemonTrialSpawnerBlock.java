package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.models.CobblemonSpawner;
import com.lemenok.cobblemontrialsedition.models.CobblemonTrialSpawnerState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
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
    public MapCodec<CobblemonTrialSpawnerBlock> codec() { return CODEC; }

    public CobblemonTrialSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(STATE, CobblemonTrialSpawnerState.INACTIVE)).setValue(OMINOUS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> arg) {
        arg.add(new Property[]{STATE, OMINOUS});
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }


    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) { return new CobblemonTrialSpawnerEntity(blockPos, blockState); }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level arg, BlockState arg2, BlockEntityType<T> arg3) {
        BlockEntityTicker var10000;
        if (arg instanceof ServerLevel serverLevel) {
            var10000 = createTickerHelper(arg3, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), (arg2x, arg3x, arg4, arg5) -> arg5.getCobblemonTrialSpawner().tickServer(serverLevel, arg3x, (Boolean)arg4.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false)));
        } else {
            var10000 = createTickerHelper(arg3, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), (argx, arg2x, arg3x, arg4) -> arg4.getCobblemonTrialSpawner().tickClient(argx, arg2x, (Boolean)arg3x.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false)));
        }

        return var10000;
    }

    @Override
    public void appendHoverText(ItemStack arg, Item.TooltipContext arg2, List<Component> list, TooltipFlag arg3) {
        super.appendHoverText(arg, arg2, list, arg3);
        CobblemonSpawner.appendHoverText(arg, list, "spawn_data");
    }

    static {
        STATE = EnumProperty.create("cobblemon_trial_spawner_state", CobblemonTrialSpawnerState.class);
        OMINOUS = BlockStateProperties.OMINOUS;
    }

    /* BLOCK ENTITY */

    /*@Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), CobblemonTrialSpawnerEntity::tick);
    }*/




}
