package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEditionFabric.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}