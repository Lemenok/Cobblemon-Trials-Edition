package com.lemenok.cobblemontrialsedition.potion;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, CobblemonTrialsEdition.MODID);

    public static final Holder<Potion> TRIAL_POTION = POTIONS.register("trial_potion",
            () -> new Potion(new MobEffectInstance(MobEffects.TRIAL_OMEN, 36000, 0)));

    public static void register(IEventBus eventBus){
        POTIONS.register(eventBus);
    }
}
