package com.lemenok.cobblemontrialsedition;

import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.client.renderer.blockentity.CobblemonTrialSpawnerRenderer;
import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.particle.UnownParticles;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class CobblemonTrialsEditionClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.UNOWN_PARTICLES, UnownParticles.Provider::new);
        BlockEntityRenderers.register(ModBlockEntities.COBBLEMON_TRIAL_SPAWNER, CobblemonTrialSpawnerRenderer::new);
        //BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COBBLEMON_TRIAL_SPAWNER, RenderType.cutout());
    }
}
