package com.lemenok.cobblemontrialsedition;

import net.neoforged.neoforge.common.ModConfigSpec;

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

    public static final ModConfigSpec.BooleanValue REPLACE_SPAWNERS_OUTSIDE_OF_STRUCTURES_WITH_DEFAULT_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement outside of structures. If set to false, this will disable the spawner replacement for any spawners outside of structures (eg. Monster_Rooms)")
            .define("replaceSpawnersInStructuresWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to replace any spawners that are not listed in configuration with a default spawner. " +
                    "This is mostly used to cover the edge cases where a spawner exists inside of a structure range but isn't replaced. " +
                    "In default-spawner.json this is the 'Minecraft:Zombie' entity spawner if the entity is not listed.")
            .define("replaceAnyUnspecifiedSpawnersWithDefaultCobblemonSpawners", true);

    static final ModConfigSpec SPEC = BUILDER.build();

}
