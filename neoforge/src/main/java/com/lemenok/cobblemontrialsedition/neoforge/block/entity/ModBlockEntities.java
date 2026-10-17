package com.lemenok.cobblemontrialsedition.neoforge.block.entity;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.neoforge.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CobblemonTrialsEdition.MODID);

    public static final Supplier<BlockEntityType<CobblemonTrialSpawnerEntity>> COBBLEMON_TRIAL_SPAWNER =
            BLOCK_ENTITIES.register("cobblemon_trial_spawner", () ->
                    BlockEntityType.Builder.of(CobblemonTrialSpawnerEntity::new,
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_VANILLA.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_MAGMA.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_DUNGEON.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_RELIC.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_END.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_PRISMARINE.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_FORTRESS.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_LABORATORY.get(),
                            ModBlocks.COBBLEMON_TRIAL_SPAWNER_CREATE.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
