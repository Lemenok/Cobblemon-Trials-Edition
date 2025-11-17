package com.lemenok.cobblemontrialsedition;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGS = BUILDER
            .comment("Whether to log data regarding the location and data stored when placing cobblemon trial spawners.")
            .define("enableDebugLogs", false);

    public static final ModConfigSpec.BooleanValue REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement at all. If set to false, this will disable all spawner replacement.")
            .define("replaceGeneratedSpawnersWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement in structures. If set to false, this will disable all spawner replacement in structures.")
            .define("replaceSpawnersNotInStructuresWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_SPAWNERS_OUTSIDE_OF_STRUCTURES_WITH_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement outside of structures. If set to false, this will disable the spawner replacement for any spawners outside of structures (eg. Monster_Rooms)")
            .define("replaceSpawnersInStructuresWithCobblemonSpawners", true);

    static final ModConfigSpec SPEC = BUILDER.build();

}
