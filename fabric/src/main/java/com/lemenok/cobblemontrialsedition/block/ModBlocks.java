package com.lemenok.cobblemontrialsedition.block;

import com.lemenok.cobblemontrialsedition.CobblemonTrialsEditionFabric;
import com.lemenok.cobblemontrialsedition.block.custom.CobblemonTrialSpawnerBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final Block COBBLEMON_TRIAL_SPAWNER = registerBlock(
            "cobblemon_trial_spawner", new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).lightLevel((blockState) ->
                    blockState.getValue(CobblemonTrialSpawnerBlock.STATE).lightLevel()).strength(50.0F).sound(SoundType.TRIAL_SPAWNER).isViewBlocking((state, world, pos) -> false).noOcclusion()));

    private static Block registerBlock(String name, Block block){
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEditionFabric.MODID, name),
                new BlockItem(block, new Item.Properties()));
    }

    public static void registerModBlocks() {
        CobblemonTrialsEditionFabric.LOGGER.info("Registering Mod Blocks for " + CobblemonTrialsEditionFabric.MODID);
    }
}
