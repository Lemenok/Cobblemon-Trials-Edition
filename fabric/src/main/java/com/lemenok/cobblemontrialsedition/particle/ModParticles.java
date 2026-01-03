package com.lemenok.cobblemontrialsedition.particle;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;


public class ModParticles {

    public static final SimpleParticleType UNOWN_PARTICLES =
            registerParticle("unown_particles", FabricParticleTypes.simple());

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name), particleType);
    }

    public static void registerParticles() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Particles for " + CobblemonTrialsEditionFabric.MODID);
    }
}
