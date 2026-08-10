package com.lemenok.cobblemontrialsedition.neoforge.events;

import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.neoforge.Config;
import com.lemenok.cobblemontrialsedition.neoforge.potion.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

@EventBusSubscriber(modid = CobblemonTrialsEdition.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onBrewingRecipeRegister(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        if (Config.ENABLE_TRIAL_POTION_RECIPE.get()) {
            builder.addMix(Potions.AWKWARD, Items.SCULK, ModPotions.TRIAL_POTION);
        }
    }
}
