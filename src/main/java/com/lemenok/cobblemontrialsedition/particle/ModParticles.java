package com.lemenok.cobblemontrialsedition.particle;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, CobblemonTrialsEdition.MODID);

    public static final Supplier<SimpleParticleType> UNOWN_PARTICLES =
            PARTICLE_TYPES.register("unown_particles", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
