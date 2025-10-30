package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.models.CobblemonTrialSpawnerRenderer;
import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.particle.UnownParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = CobblemonTrialsEdition.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.COBBLEMON_TRIAL_SPAWNER.get(), CobblemonTrialSpawnerRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.UNOWN_PARTICLES.get(), UnownParticles.Provider::new);
    }
}
