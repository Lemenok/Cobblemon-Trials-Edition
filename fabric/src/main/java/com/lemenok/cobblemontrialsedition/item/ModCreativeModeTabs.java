package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final CreativeModeTab COBBLEMON_TRIALS_EDITION_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, "cobblemon_trials_edition_blocks_tab"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.COBBLEMON_TRIAL_SPAWNER))
                    .title(Component.translatable("Cobblemon Trials Edition Blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER);
                        //output.accept(ModPotions.TRIAL_POTION);
                    })
                    .build());


    public static void registerItemGroups() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Item Groups for " + CobblemonTrialsEditionFabric.MODID);
    }
}
