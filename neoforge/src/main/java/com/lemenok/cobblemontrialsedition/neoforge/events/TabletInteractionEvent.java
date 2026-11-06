package com.lemenok.cobblemontrialsedition.neoforge.events;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import com.lemenok.cobblemontrialsedition.item.MysteriousTabletItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState; // New TriState import
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


@EventBusSubscriber(modid = CobblemonTrialsEdition.MODID)
public class TabletInteractionEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();

        // Check if the player is holding the tablet
        if (stack.getItem() instanceof MysteriousTabletItem) {
            // Check if they are clicking the spawner
            if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof CobblemonTrialSpawnerBlock) {

                // FALSE prevents the block's native right-click (the menu) from opening
                event.setUseBlock(TriState.FALSE);

                // TRUE forces the item's useOn method to execute
                event.setUseItem(TriState.TRUE);
            }
        }
    }
}
