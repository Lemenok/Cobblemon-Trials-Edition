package com.lemenok.cobblemontrialsedition.block;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerState;
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

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CobblemonTrialsEdition.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    /*public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER  = registerBlock("cobblemontrialspawner",
            () -> new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().noCollission()));*/

    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER = registerBlock(
            "cobblemon_trial_spawner", () -> new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).lightLevel((arg) ->
                    ((CobblemonTrialSpawnerState)arg.getValue(CobblemonTrialSpawnerBlock.STATE)).lightLevel()).strength(50.0F).sound(SoundType.TRIAL_SPAWNER).isViewBlocking((state, world, pos) -> false).noOcclusion()));

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
