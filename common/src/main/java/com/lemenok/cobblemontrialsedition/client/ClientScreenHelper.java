package com.lemenok.cobblemontrialsedition.client;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.screen.TrialSpawnerConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientScreenHelper {
    public static void openTrialSpawnerScreen(BlockPos pos, SpawnerProperties properties) {
        Minecraft client = Minecraft.getInstance();

        // Push the screen execution to the main client render thread
        client.execute(() -> {
            client.setScreen(new TrialSpawnerConfigScreen(pos, properties));
        });
        //Minecraft.getInstance().setScreen(new TrialSpawnerConfigScreen(pos, properties));
    }
}
