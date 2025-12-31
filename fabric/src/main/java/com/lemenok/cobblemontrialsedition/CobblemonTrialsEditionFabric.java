package com.lemenok.cobblemontrialsedition;

import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.ModBlockEntities;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.events.SpawnerReplacementHandler;
import com.lemenok.cobblemontrialsedition.item.ModCreativeModeTabs;
import com.lemenok.cobblemontrialsedition.particle.ModParticles;
import com.lemenok.cobblemontrialsedition.potion.ModPotions;
import com.lemenok.cobblemontrialsedition.sound.ModSounds;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonTrialsEditionFabric implements ModInitializer {
    public static final String MODID = "cobblemontrialsedition";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final SpawnerReplacementHandler SPAWNER_REPLACEMENT_HANDLER = new SpawnerReplacementHandler();

    public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_STRUCTURE_REGISTRY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID, "structures")
            );

    public static final ResourceKey<Registry<StructureProperties>> COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID, "features")
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

        AutoConfig.register(Config.class, Toml4jConfigSerializer::new);

        // Register Potion Recipe
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(Items.SCULK), ModPotions.TRIAL_POTION);
        });

        // Register Datapacks
        DynamicRegistries.register(COBBLEMON_TRIALS_STRUCTURE_REGISTRY, StructureProperties.CODEC);
        DynamicRegistries.register(COBBLEMON_TRIALS_DEFAULT_STRUCTURE_REGISTRY, StructureProperties.CODEC);
        DynamicRegistries.register(COBBLEMON_TRIALS_LOOT_TABLE_REGISTRY, LootTable.DIRECT_CODEC);

        // Handle Chunk Load Events
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) ->
                world.getServer().execute(() -> SPAWNER_REPLACEMENT_HANDLER.processNewChunk(world, chunk)));
    }

    /*
    @EventBusSubscriber(modid = MODID)
    public static class ClientModEvents {



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
    }*/
}
