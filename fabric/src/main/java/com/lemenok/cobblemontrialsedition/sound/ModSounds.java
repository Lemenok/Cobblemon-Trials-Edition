package com.lemenok.cobblemontrialsedition.sound;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent COBBLEMON_TRIAL_SPAWNER_AMBIENT = registerSoundEvent("cobblemon_trial_spawner_ambient");
    public static final SoundEvent COBBLEMON_TRIAL_SPAWNER_AMBIENT_OMINOUS = registerSoundEvent("cobblemon_trial_spawner_ambient_ominous");


    private static SoundEvent registerSoundEvent(String name){
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(resourceLocation);

        return Registry.register(BuiltInRegistries.SOUND_EVENT, resourceLocation, event);
    }

    public static void registerSounds() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Mod Sounds for" + CobblemonTrialsEditionFabric.MODID);
    }
}
