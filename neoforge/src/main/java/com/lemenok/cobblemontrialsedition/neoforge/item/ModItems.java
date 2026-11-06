package com.lemenok.cobblemontrialsedition.neoforge.item;

import com.lemenok.cobblemontrialsedition.item.MysteriousTabletItem;
import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEdition.MODID);

    public static final DeferredItem<MysteriousTabletItem> MYSTERIOUS_TABLET = ITEMS.registerItem(
            "mysterious_tablet",
            properties -> new MysteriousTabletItem(properties)
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}