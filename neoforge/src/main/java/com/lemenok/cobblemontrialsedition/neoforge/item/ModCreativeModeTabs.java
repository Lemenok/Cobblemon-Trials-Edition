package com.lemenok.cobblemontrialsedition.neoforge.item;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.neoforge.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
                        output.accept(ModItems.MYSTERIOUS_TABLET.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_VANILLA.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_MAGMA.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_DUNGEON.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_RELIC.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_END.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_PRISMARINE.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_FORTRESS.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_LABORATORY.get());
                        output.accept(ModBlocks.COBBLEMON_TRIAL_SPAWNER_CREATE.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
