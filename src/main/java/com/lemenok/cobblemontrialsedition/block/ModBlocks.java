package com.lemenok.cobblemontrialsedition.block;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CobblemonTrialsEdition.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    public static final DeferredBlock<Block> COBBLEMON_TRIAL_SPAWNER  = registerBlock("cobblemontrialspawner",
            () -> new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().noCollission()));

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
