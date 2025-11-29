package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.item.Item;

public class ModItems {



    //public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonTrialsEditionFabric.MODID);

    //public static void register(IEventBus eventBus) {
    //    ITEMS.register(eventBus);
    //}

    public static void registerModItems() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Mod Items for " + CobblemonTrialsEditionFabric.MODID);
    }

    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of, item);
    }
}