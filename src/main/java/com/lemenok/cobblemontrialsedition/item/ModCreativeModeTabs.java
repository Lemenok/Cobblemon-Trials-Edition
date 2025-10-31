package com.lemenok.cobblemontrialsedition.item;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CobblemonTrialsEdition.MODID);

    public static final Supplier<CreativeModeTab> COBBLEMON_TRIALS_EDITION_BLOCKS_TAB = CREATIVE_MODE_TAB.register("cobblemon_trials_edition_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.COBBLEMON_TRIAL_SPAWNER))
                    .title(Component.translatable("Cobblemon Trials Edition Blocks"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER.get());

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
