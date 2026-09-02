package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonSpawner;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
import com.lemenok.cobblemontrialsedition.client.ClientScreenHelper;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.screen.SpawnerNbtParser;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CobblemonTrialSpawnerBlock extends BaseEntityBlock {
    public static final MapCodec<CobblemonTrialSpawnerBlock> CODEC = simpleCodec(CobblemonTrialSpawnerBlock::new);
    public static final EnumProperty<CobblemonTrialSpawnerState> STATE;
    public static final BooleanProperty OMINOUS;

    @Override
    public @NotNull MapCodec<CobblemonTrialSpawnerBlock> codec() { return CODEC; }

    public CobblemonTrialSpawnerBlock(Properties properties) {
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
            blockEntityTicker = createTickerHelper(blockEntityType, Services.PLATFORM.getCobblemonTrialSpawnerBlockEntity(),
                    (level1, blockPos, blockState1, cobblemonTrialSpawnerEntity) ->
                            cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().tickServer(serverLevel, blockPos,
                                    blockState1.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false)));
        } else {
            blockEntityTicker = createTickerHelper(blockEntityType, Services.PLATFORM.getCobblemonTrialSpawnerBlockEntity(),
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

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Validate permissions
            if (!serverPlayer.hasPermissions(2)) {
                return InteractionResult.PASS;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CobblemonTrialSpawnerEntity spawnerEntity) {
                try {
                    CompoundTag fullNbt = blockEntity.saveWithFullMetadata(level.registryAccess());
                    var properties = SpawnerNbtParser.parse(spawnerEntity.getCobblemonTrialSpawner(), fullNbt);
                    BlockPos blockPos = new BlockPos(fullNbt.getInt("x"),fullNbt.getInt("y"),fullNbt.getInt("z"));

                    List<ResourceLocation> allLootTables = new ArrayList<>();

                    if (level.getServer() != null) {
                        allLootTables.addAll(level.getServer().reloadableRegistries().get()
                                .lookupOrThrow(Registries.LOOT_TABLE)
                                .listElementIds()
                                .map(ResourceKey::location)
                                .toList());

                        allLootTables.addAll(level.getServer().reloadableRegistries().get()
                                .lookupOrThrow(Services.PLATFORM.getLootTableRegistry())
                                .listElementIds()
                                .map(ResourceKey::location)
                                .toList());
                    }

                    Services.PLATFORM.sendSpawnerConfigPacket(serverPlayer, blockPos, properties, allLootTables);
                    ClientScreenHelper.openTrialSpawnerScreen(blockPos, properties, allLootTables);

                } catch (Exception e) {
                    System.out.println("SERVER: Failed to parse NBT or send packet!");
                    e.printStackTrace();
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof CobblemonTrialSpawnerEntity spawnerEntity) {
            return switch (spawnerEntity.getCobblemonTrialSpawner().getState()) {
                case INACTIVE -> 0;
                case WAITING_FOR_PLAYERS -> 0;
                case ACTIVE -> 5;
                case WAITING_FOR_REWARD_EJECTION -> 10;
                case EJECTING_REWARD -> 10;
                case COOLDOWN -> 15;
            };
        }
        return 0;
    }

    static {
        STATE = EnumProperty.create("cobblemon_trial_spawner_state", CobblemonTrialSpawnerState.class);
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}
