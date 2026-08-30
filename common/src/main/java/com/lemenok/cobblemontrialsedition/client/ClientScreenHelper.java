package com.lemenok.cobblemontrialsedition.client;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.screen.TrialSpawnerConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ClientScreenHelper {
    public static void openTrialSpawnerScreen(BlockPos pos, SpawnerProperties properties, List<ResourceLocation> availableLootTables) {
        Minecraft client = Minecraft.getInstance();

        // Push the screen execution to the main client render thread
        client.execute(() -> {
            client.setScreen(new TrialSpawnerConfigScreen(pos, properties, availableLootTables));
        });
        //Minecraft.getInstance().setScreen(new TrialSpawnerConfigScreen(pos, properties));
    }
}
