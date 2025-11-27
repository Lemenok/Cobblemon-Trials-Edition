package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}