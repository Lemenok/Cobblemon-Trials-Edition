package com.lemenok.cobblemontrialsedition;

import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.events.SpawnerReplacementHandler;
import com.lemenok.cobblemontrialsedition.item.ModCreativeModeTabs;
import com.lemenok.cobblemontrialsedition.item.ModItems;
import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.potion.ModPotions;
import com.lemenok.cobblemontrialsedition.sound.ModSounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CobblemonTrialsEdition.MODID)
public class CobblemonTrialsEdition {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cobblemontrialsedition";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CobblemonTrialsEdition(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CobblemonTrialsEdition) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SpawnerReplacementHandler());

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);
        ModPotions.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            //event.accept(ModItems.CHARCADET_TRIAL_KEY);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class ClientModEvents {

        public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_STRUCTURE_REGISTRY =
                ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, "structures")
                );

        public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY =
                ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, "features")
                );

        public static final ResourceKey<Registry<LootTable>> COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY =
                ResourceKey.createRegistryKey(
                        ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, "loot_table")
                );

        @SubscribeEvent
        public static void addRegistries(DataPackRegistryEvent.NewRegistry event){
            event.dataPackRegistry(
                    COBBLEMON_TRIALS_STRUCTURE_REGISTRY,
                    StructureProperties.CODEC,
                    StructureProperties.CODEC,
                    builder -> builder.maxId(256)
            );
            event.dataPackRegistry(
                    COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY,
                    StructureProperties.CODEC,
                    StructureProperties.CODEC,
                    builder -> builder.maxId(256)
            );
            event.dataPackRegistry(
                    COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY,
                    LootTable.DIRECT_CODEC,
                    LootTable.DIRECT_CODEC,
                    builder -> builder.maxId(256)
            );
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
