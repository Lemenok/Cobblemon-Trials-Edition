package com.lemenok.cobblemontrialsedition.fabric.item;

import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.item.MysteriousTabletItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item MYSTERIOUS_TABLET = registerItem("mysterious_tablet", new MysteriousTabletItem(new Item.Properties()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name), item);
    }

    public static void registerItems() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Items for " + CobblemonTrialsEditionFabric.MODID);
    }
}
