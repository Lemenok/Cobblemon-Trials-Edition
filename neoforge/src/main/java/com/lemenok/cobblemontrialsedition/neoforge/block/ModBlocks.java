package com.lemenok.cobblemontrialsedition.neoforge.block;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CobblemonTrialsEdition.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER = registerCobblemonTrialSpawner("cobblemon_trial_spawner");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_VANILLA = registerCobblemonTrialSpawner("cobblemon_trial_spawner_vanilla");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_MAGMA = registerCobblemonTrialSpawner("cobblemon_trial_spawner_magma");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_DUNGEON = registerCobblemonTrialSpawner("cobblemon_trial_spawner_dungeon");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_RELIC = registerCobblemonTrialSpawner("cobblemon_trial_spawner_relic");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_END = registerCobblemonTrialSpawner("cobblemon_trial_spawner_end");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_PRISMARINE = registerCobblemonTrialSpawner("cobblemon_trial_spawner_prismarine");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_FORTRESS = registerCobblemonTrialSpawner("cobblemon_trial_spawner_fortress");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_LABORATORY = registerCobblemonTrialSpawner("cobblemon_trial_spawner_laboratory");
    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER_CREATE = registerCobblemonTrialSpawner("cobblemon_trial_spawner_create");

    private static @NotNull DeferredBlock<Block> registerCobblemonTrialSpawner(String name) {
        return registerBlock(
                name, () -> new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).lightLevel((blockState) ->
                        blockState.getValue(CobblemonTrialSpawnerBlock.STATE).lightLevel()).strength(50.0F).sound(SoundType.TRIAL_SPAWNER).isViewBlocking((state, world, pos) -> false).noOcclusion()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
