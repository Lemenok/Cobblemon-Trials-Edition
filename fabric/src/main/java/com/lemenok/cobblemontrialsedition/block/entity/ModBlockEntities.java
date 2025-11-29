package com.lemenok.cobblemontrialsedition.block.entity;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CobblemonTrialsEditionFabric.MODID);

    public static final Supplier<BlockEntityType<CobblemonTrialSpawnerEntity>> COBBLEMON_TRIAL_SPAWNER =
            BLOCK_ENTITIES.register("cobblemon_trial_spawner", () ->
                    BlockEntityType.Builder.of(CobblemonTrialSpawnerEntity::new, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
