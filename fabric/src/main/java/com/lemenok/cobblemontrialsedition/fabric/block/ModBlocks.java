package com.lemenok.cobblemontrialsedition.fabric.block;

import com.lemenok.cobblemontrialsedition.fabric.CobblemonTrialsEditionFabric;
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
import org.jetbrains.annotations.NotNull;

public class ModBlocks {

    public static final Block COBBLEMON_TRIAL_SPAWNER = registerCobblemonTrialSpawner("cobblemon_trial_spawner");
    public static final Block COBBLEMON_TRIAL_SPAWNER_VANILLA = registerCobblemonTrialSpawner("cobblemon_trial_spawner_vanilla");
    public static final Block COBBLEMON_TRIAL_SPAWNER_MAGMA = registerCobblemonTrialSpawner("cobblemon_trial_spawner_magma");
    public static final Block COBBLEMON_TRIAL_SPAWNER_DUNGEON = registerCobblemonTrialSpawner("cobblemon_trial_spawner_dungeon");
    public static final Block COBBLEMON_TRIAL_SPAWNER_RELIC = registerCobblemonTrialSpawner("cobblemon_trial_spawner_relic");
    public static final Block COBBLEMON_TRIAL_SPAWNER_END = registerCobblemonTrialSpawner("cobblemon_trial_spawner_end");
    public static final Block COBBLEMON_TRIAL_SPAWNER_PRISMARINE = registerCobblemonTrialSpawner("cobblemon_trial_spawner_prismarine");
    public static final Block COBBLEMON_TRIAL_SPAWNER_FORTRESS = registerCobblemonTrialSpawner("cobblemon_trial_spawner_fortress");
    public static final Block COBBLEMON_TRIAL_SPAWNER_LABORATORY = registerCobblemonTrialSpawner("cobblemon_trial_spawner_laboratory");
    public static final Block COBBLEMON_TRIAL_SPAWNER_CREATE = registerCobblemonTrialSpawner("cobblemon_trial_spawner_create");

    private static @NotNull Block registerCobblemonTrialSpawner(String name) {
        return registerBlock(
                name,
                new CobblemonTrialSpawnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).lightLevel((blockState) ->
                        blockState.getValue(CobblemonTrialSpawnerBlock.STATE).lightLevel()).strength(50.0F).sound(SoundType.TRIAL_SPAWNER).isViewBlocking((state, world, pos) -> false).noOcclusion())
        );
    }

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
