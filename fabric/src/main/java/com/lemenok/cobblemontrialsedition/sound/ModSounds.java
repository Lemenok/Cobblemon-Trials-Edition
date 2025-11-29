package com.lemenok.cobblemontrialsedition.sound;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CobblemonTrialsEditionFabric.MODID);

    public static final Supplier<SoundEvent> COBBLEMON_TRIAL_SPAWNER_AMBIENT = registerSoundEvent("cobblemon_trial_spawner_ambient");
    public static final Supplier<SoundEvent> COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS = registerSoundEvent("cobblemon_trial_spawner_ambient_ominous");

    private static Supplier<SoundEvent> registerSoundEvent(String name){
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
