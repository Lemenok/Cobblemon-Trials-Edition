package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MysteriousTabletItem extends Item {

    private static final String MODE_KEY = "TabletMode";
    private static final String COPIED_DATA_KEY = "CopiedSpawnerData";

    // Mode Constants
    private static final int MODE_COPY_PASTE = 0;
    private static final int MODE_CLEAR = 1;
    private static final int MODE_EJECT = 2;
    private static final int MODE_RESET = 3;

    public MysteriousTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.contains(COPIED_DATA_KEY) || super.isFoil(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            handleTabletCycleOrClear(player, stack);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        boolean isCobblemonSpawner = state.getBlock() instanceof CobblemonTrialSpawnerBlock;

        if (isCobblemonSpawner) {
            // We only process logic on the server side, but we must return a SUCCESS result
            // on the client side as well to prevent the block from being interacted with.
            if (!level.isClientSide()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof CobblemonTrialSpawnerEntity spawnerEntity) {
                    if (player.isShiftKeyDown()) {
                        if (getMode(stack) == MODE_COPY_PASTE) {
                            copySpawnerData(stack, spawnerEntity, level, player);
                        }
                    } else {
                        executeModeAction(stack, spawnerEntity, level, player);
                    }
                }
            }

            // By returning SUCCESS (or CONSUME/sidedSuccess) here for BOTH client and server
            // when clicking on the spawner, we tell the game the item handled the click,
            // preventing the block's use method (the menu) from opening.
            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            // If we didn't click a spawner, handle the normal shift-click cycle.
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    handleTabletCycleOrClear(player, stack);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // If it's not a spawner and we aren't shift clicking, pass the interaction on.
        return InteractionResult.PASS;
    }

    private void handleTabletCycleOrClear(Player player, ItemStack stack) {
        int currentMode = getMode(stack);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (currentMode == MODE_COPY_PASTE && customData.contains(COPIED_DATA_KEY)) {
            // Clear copied data
            customData = customData.update(tag -> tag.remove(COPIED_DATA_KEY));
            stack.set(DataComponents.CUSTOM_DATA, customData);
            player.displayClientMessage(Component.literal("Cleared").withStyle(ChatFormatting.YELLOW), true);
        } else {
            // Cycle Mode
            int newMode = (currentMode + 1) % 4;
            customData = customData.update(tag -> tag.putInt(MODE_KEY, newMode));
            stack.set(DataComponents.CUSTOM_DATA, customData);

            String modeName = switch (newMode) {
                case MODE_COPY_PASTE -> "Copy and Paste";
                case MODE_CLEAR -> "Clear";
                case MODE_EJECT -> "Eject";
                case MODE_RESET -> "Reset";
                default -> "Unknown";
            };

            player.displayClientMessage(Component.literal("Mode: " + modeName).withStyle(ChatFormatting.AQUA), true);
        }
    }

    private void executeModeAction(ItemStack stack, CobblemonTrialSpawnerEntity spawnerEntity, Level level, Player player) {
        int mode = getMode(stack);
        switch (mode) {
            case MODE_COPY_PASTE -> pasteSpawnerData(stack, spawnerEntity, level, player);
            case MODE_CLEAR -> clearSpawner(spawnerEntity, player);
            case MODE_EJECT -> ejectSpawner(spawnerEntity, level, player);
            case MODE_RESET -> resetSpawner(spawnerEntity, player);
        }
    }

    private int getMode(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.contains(MODE_KEY) ? customData.copyTag().getInt(MODE_KEY) : MODE_COPY_PASTE;
    }

    private void copySpawnerData(ItemStack stack, CobblemonTrialSpawnerEntity spawnerEntity, Level level, Player player) {
        CompoundTag fullNbt = spawnerEntity.saveWithFullMetadata(level.registryAccess());
        CompoundTag dataToCopy = new CompoundTag();

        // Extracting configuration logic from standard NBT generation
        if (fullNbt.contains(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME)) {
            dataToCopy.put(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME, fullNbt.get(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME).copy());
        }
        if (fullNbt.contains(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME)) {
            dataToCopy.put(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME, fullNbt.get(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME).copy());
        }
        if (fullNbt.contains("target_cooldown_length")) {
            dataToCopy.putInt("target_cooldown_length", fullNbt.getInt("target_cooldown_length"));
        }
        if (fullNbt.contains("required_player_range")) {
            dataToCopy.putInt("required_player_range", fullNbt.getInt("required_player_range"));
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(tag -> tag.put(COPIED_DATA_KEY, dataToCopy));
        stack.set(DataComponents.CUSTOM_DATA, customData);

        player.displayClientMessage(Component.literal("Copied").withStyle(ChatFormatting.GREEN), true);
    }

    private void pasteSpawnerData(ItemStack stack, CobblemonTrialSpawnerEntity spawnerEntity, Level level, Player player) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.contains(COPIED_DATA_KEY)) return;

        CompoundTag copiedData = customData.copyTag().getCompound(COPIED_DATA_KEY);
        CompoundTag currentNbt = spawnerEntity.saveWithFullMetadata(level.registryAccess());

        if (copiedData.contains(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME)) {
            currentNbt.put(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME, copiedData.get(CobblemonTrialSpawner.NORMAL_CONFIG_TAG_NAME).copy());
        }
        if (copiedData.contains(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME)) {
            currentNbt.put(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME, copiedData.get(CobblemonTrialSpawner.OMINOUS_CONFIG_TAG_NAME).copy());
        }
        if (copiedData.contains("target_cooldown_length")) {
            currentNbt.putInt("target_cooldown_length", copiedData.getInt("target_cooldown_length"));
        }
        if (copiedData.contains("required_player_range")) {
            currentNbt.putInt("required_player_range", copiedData.getInt("required_player_range"));
        }

        spawnerEntity.loadAdditional(currentNbt, level.registryAccess());

        // This naturally sets the state back to WAITING_FOR_PLAYERS and despawns current tracked Pokemon
        spawnerEntity.resetSpawnerData(spawnerEntity.getCobblemonTrialSpawner().getData(), spawnerEntity.getCobblemonTrialSpawner());

        player.displayClientMessage(Component.literal("Pasted").withStyle(ChatFormatting.GREEN), true);
    }

    private void clearSpawner(CobblemonTrialSpawnerEntity spawnerEntity, Player player) {
        var spawner = spawnerEntity.getCobblemonTrialSpawner();

        // Resets the configs to default, mimicking a newly placed spawner
        spawner.setConfig(CobblemonTrialSpawnerConfig.DEFAULT, false);
        spawner.setConfig(CobblemonTrialSpawnerConfig.DEFAULT, true);

        spawnerEntity.resetSpawnerData(spawner.getData(), spawner);

        player.displayClientMessage(Component.literal("Spawner Cleared").withStyle(ChatFormatting.YELLOW), true);
    }

    private void ejectSpawner(CobblemonTrialSpawnerEntity spawnerEntity, Level level, Player player) {
        // Setting the state to WAITING_FOR_REWARD_EJECTION kicks off the ejecting reward state on the next tick[cite: 6]
        // This ultimately runs ejectReward() and transitions the spawner into COOLDOWN.[cite: 3, 6]
        spawnerEntity.setState(level, CobblemonTrialSpawnerState.WAITING_FOR_REWARD_EJECTION);
        player.displayClientMessage(Component.literal("Ejecting Loot").withStyle(ChatFormatting.GOLD), true);
    }

    private void resetSpawner(CobblemonTrialSpawnerEntity spawnerEntity, Player player) {
        // Internal data clearance, active pokemon despawning, and setting to WAITING_FOR_PLAYERS is handled securely here[cite: 7]
        spawnerEntity.resetSpawnerData(spawnerEntity.getCobblemonTrialSpawner().getData(), spawnerEntity.getCobblemonTrialSpawner());
        player.displayClientMessage(Component.literal("Spawner Reset").withStyle(ChatFormatting.AQUA), true);
    }
}