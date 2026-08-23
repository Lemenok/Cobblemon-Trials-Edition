package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonSpawner;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
import com.lemenok.cobblemontrialsedition.client.ClientScreenHelper;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Only open the screen on the logical client
        if (level.isClientSide) {

            var block = level.getBlockEntity(pos);

            // 1. Get your custom BlockEntity
            if (block instanceof CobblemonTrialSpawnerEntity spawnerEntity) {

                // 2. Fetch the current properties to pass to the screen
                SpawnerProperties emptySpawnerProps = new SpawnerProperties(
                        List.of(), // blockTypesToReplace
                        List.of(), // mobEntitiesInSpawnerToReplace
                        40,        // ticksBetweenSpawnAttempts
                        36000,     // spawnerCooldown
                        14,        // playerDetectionRange
                        4,         // spawnRange
                        2,         // maximumNumberOfSimultaneousPokemon
                        1,         // maximumNumberOfSimultaneousPokemonAddedPerPlayer
                        4,         // totalNumberOfPokemonPerTrial
                        1,         // totalNumberOfPokemonPerTrialAddedPerPlayer
                        List.of(), // lootTables
                        List.of(), // ominousLootTables
                        false,     // ominousSpawnerAttacksEnabled
                        true,      // doPokemonSpawnedGlow
                        List.of(), // listOfPokemonToSpawn
                        List.of()  // listOfOminousPokemonToSpawn
                );

                // 3. Open the screen safely
                ClientScreenHelper.openTrialSpawnerScreen(emptySpawnerProps);
            }
        }

        // Return SUCCESS to consume the right-click action
        return InteractionResult.SUCCESS;
    }

    static {
        STATE = EnumProperty.create("cobblemon_trial_spawner_state", CobblemonTrialSpawnerState.class);
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}
