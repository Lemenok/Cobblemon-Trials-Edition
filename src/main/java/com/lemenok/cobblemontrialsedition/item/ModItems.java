package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEdition.MODID);


    /*public static final DeferredItem<Item> CHARCADET_TRIAL_KEY = ITEMS.register("charcadet_trial_key",
            () -> new Item(new Item.Properties()));*/

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}