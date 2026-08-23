package com.lemenok.cobblemontrialsedition.client;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.screen.TrialSpawnerConfigScreen;
import net.minecraft.client.Minecraft;

public class ClientScreenHelper {
    public static void openTrialSpawnerScreen(SpawnerProperties properties) {
        Minecraft.getInstance().setScreen(new TrialSpawnerConfigScreen(properties));
    }
}
