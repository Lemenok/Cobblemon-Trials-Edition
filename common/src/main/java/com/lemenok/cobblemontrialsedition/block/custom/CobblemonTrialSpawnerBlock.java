package com.lemenok.cobblemontrialsedition.block.custom;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonSpawner;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
import com.lemenok.cobblemontrialsedition.client.ClientScreenHelper;
import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.screen.SpawnerNbtParser;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /*
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
                ClientScreenHelper.openTrialSpawnerScreen(pos, emptySpawnerProps);
            }
        }

        // Return SUCCESS to consume the right-click action
        return InteractionResult.SUCCESS;
    }*/

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Validate permissions (optional, but recommended so standard players can't open it)
            if (!serverPlayer.hasPermissions(2)) {
                return InteractionResult.PASS;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof CobblemonTrialSpawnerEntity spawnerEntity) {
                try {
                    CompoundTag fullNbt = blockEntity.saveWithFullMetadata(level.registryAccess());
                    var properties = SpawnerNbtParser.parse(spawnerEntity.getCobblemonTrialSpawner(), fullNbt);
                    BlockPos blockPos = new BlockPos(fullNbt.getInt("x"),fullNbt.getInt("y"),fullNbt.getInt("z"));

                    List<ResourceLocation> allLootTables = new java.util.ArrayList<>();

                    if (level.getServer() != null) {
                        allLootTables.addAll(level.getServer().reloadableRegistries().get()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE)
                                .listElementIds()
                                .map(net.minecraft.resources.ResourceKey::location)
                                .toList());

                        allLootTables.addAll(level.getServer().reloadableRegistries().get()
                                .lookupOrThrow(Services.PLATFORM.getLootTableRegistry())
                                .listElementIds()
                                .map(net.minecraft.resources.ResourceKey::location)
                                .toList());
                    }

                    Services.PLATFORM.sendSpawnerConfigPacket(serverPlayer, blockPos, properties, allLootTables);

                    ClientScreenHelper.openTrialSpawnerScreen(blockPos, properties, allLootTables);

                    // Send your packet here
                    // e.g., PacketDistributor.sendToPlayer(serverPlayer, new OpenSpawnerConfigS2CPacket(pos, properties));
                } catch (Exception e) {
                    System.out.println("SERVER: Failed to parse NBT or send packet!");
                    e.printStackTrace();
                }

                // 3. Send packet to the specific player who clicked
                // Implement your modding API's packet sending here (e.g., NeoForge/Fabric specific)
                // PacketDistributor.sendToPlayer(serverPlayer, new OpenSpawnerConfigS2CPacket(pos, properties));
            }
            return InteractionResult.SUCCESS; // SERVER SUCCESS
        }

        // On the server side, just return SUCCESS so the animation triggers properly
        return InteractionResult.SUCCESS;
    }

    static {
        STATE = EnumProperty.create("cobblemon_trial_spawner_state", CobblemonTrialSpawnerState.class);
        OMINOUS = BlockStateProperties.OMINOUS;
    }
}
