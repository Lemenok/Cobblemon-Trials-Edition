package com.lemenok.cobblemontrialsedition;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi{

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if(FabricLoader.getInstance().isModLoaded("modmenu")) {
            return parent -> AutoConfig.getConfigScreen(Config.class, parent).get();
        }

        return null;
    }

}
