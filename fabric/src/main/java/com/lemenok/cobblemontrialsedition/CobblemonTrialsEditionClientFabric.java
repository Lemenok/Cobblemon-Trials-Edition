package com.lemenok.cobblemontrialsedition;

import net.fabricmc.api.ClientModInitializer;

public class CobblemonTrialsEditionClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

    }
}


/*

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CobblemonTrialsEditionFabric.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
public class CobblemonTrialsEditionClient {
    public CobblemonTrialsEditionClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

 */