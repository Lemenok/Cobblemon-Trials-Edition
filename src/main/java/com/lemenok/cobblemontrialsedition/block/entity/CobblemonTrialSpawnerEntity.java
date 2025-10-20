package com.lemenok.cobblemontrialsedition.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class CobblemonTrialSpawnerEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TrialSpawnerBlockEntity trialSpawnerBlockEntity;

    public CobblemonTrialSpawnerEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), blockPos, blockState);
        this.trialSpawnerBlockEntity = new TrialSpawnerBlockEntity(blockPos, Blocks.TRIAL_SPAWNER.defaultBlockState());
    }

    public TrialSpawnerBlockEntity getTrialSpawnerBlockEntity() {
        return this.trialSpawnerBlockEntity;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CobblemonTrialSpawnerEntity blockEntity) {
        if (blockEntity.trialSpawnerBlockEntity != null) {
            blockEntity.trialSpawnerBlockEntity.getTrialSpawner().tickClient(level, pos, blockEntity.trialSpawnerBlockEntity.getTrialSpawner().isOminous());
            blockEntity.setChanged();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(nbt, registries);
        // Save the vanilla spawner's data inside a custom tag
        CompoundTag vanillaNbt = new CompoundTag();
        this.trialSpawnerBlockEntity.loadWithComponents(vanillaNbt, registries);
        nbt.put("TrialSpawnerData", vanillaNbt);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("TrialSpawnerData")) {
            // Load the vanilla spawner's data from our custom tag
            this.trialSpawnerBlockEntity.loadWithComponents(nbt.getCompound("TrialSpawnerData"), registries);
        }
    }

    /*public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.TRIAL_SPAWNER, pos, state);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector playerdetector$entityselector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        this.trialSpawner = new TrialSpawner(this, playerdetector, playerdetector$entityselector);
    }*/

    /*
    // Save your custom data alongside the vanilla data.
    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        // Your custom logic to save data here
    }

    // Load your custom data. This is called when the Block Entity is loaded from NBT.
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        // Your custom logic to load data here
    }*/
}
