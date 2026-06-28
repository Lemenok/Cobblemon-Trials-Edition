package com.lemenok.cobblemontrialsedition.neoforge.item;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}