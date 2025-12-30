package com.lemenok.cobblemontrialsedition.potion;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public class ModPotions {

    public static final Holder<Potion> TRIAL_POTION = registerPotion("trial_potion",
            new Potion(new MobEffectInstance(MobEffects.TRIAL_OMEN, 36000, 0)));

    private static Holder<Potion> registerPotion(String name, Potion potion){
        return Registry.registerForHolder(BuiltInRegistries.POTION, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name), potion);
    }

    public static void registerPotions() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Potions for " + CobblemonTrialsEditionFabric.MODID);
    }
}
