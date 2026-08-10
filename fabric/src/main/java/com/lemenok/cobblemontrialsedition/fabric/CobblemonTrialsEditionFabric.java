package com.lemenok.cobblemontrialsedition.fabric;

import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.fabric.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.fabric.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.fabric.item.ModCreativeModeTabs;
import com.lemenok.cobblemontrialsedition.fabric.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.fabric.potion.ModPotions;
import com.lemenok.cobblemontrialsedition.fabric.processors.ModProcessors;
import com.lemenok.cobblemontrialsedition.fabric.sound.ModSounds;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonTrialsEditionFabric implements ModInitializer {
    public static final String MODID = "cobblemontrialsedition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_STRUCTURE_REGISTRY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID, "structures")
            );

    public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID, "defaults")
            );

    public static final ResourceKey<Registry<LootTable>> COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID, "loot_table")
            );


    @Override
    public void onInitialize() {
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModParticles.registerParticles();
        ModPotions.registerPotions();
        ModSounds.registerSounds();
        ModCreativeModeTabs.registerItemGroups();
        ModProcessors.register();

        Config config = AutoConfig.register(Config.class, Toml4jConfigSerializer::new).getConfig();

        // Register Potion Recipe
        if (config.ENABLE_TRIAL_POTION_RECIPE) {
            FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
                builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(Items.SCULK), ModPotions.TRIAL_POTION);
            });
        }

        // Register Datapacks
        DynamicRegistries.register(COBBLEMON_TRIALS_STRUCTURE_REGISTRY, StructureProperties.CODEC);
        DynamicRegistries.register(COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY, StructureProperties.CODEC);
        DynamicRegistries.register(COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY, LootTable.DIRECT_CODEC);

        // Build Datapack Cache on server load.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PropertiesCache.rebuild(server.registryAccess());
        });

        // Rebuild whenever a server admin runs /reload
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            // Only rebuild if the reload didn't fail due to broken JSON
            if (success) {
                PropertiesCache.rebuild(server.registryAccess());
            }
        });
    }
}
