package com.lemenok.cobblemontrialsedition.sound;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Objects;
import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CobblemonTrialsEdition.MODID);

    public static final Supplier<SoundEvent> COBBLEMON_TRIAL_SPAWNER_AMBIENT = registerSoundEvent("cobblemon_trial_spawner_ambient");
    public static final Supplier<SoundEvent> COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS = registerSoundEvent("cobblemon_trial_spawner_ambient_ominous");

    private static Supplier<SoundEvent> registerSoundEvent(String name){
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
