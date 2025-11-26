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

    public static final ModConfigSpec.BooleanValue ALLOW_SPAWNED_POKEMON_TO_BE_CATCHABLE = BUILDER
            .comment("This allows Spawned Pokemon to be Catchable. If set to True, regardless of spawner settings, Pokemon will always be catchable." +
                    "If set to false whether Spawned Pokemon can be catchable or not is left up to the Spawner's Settings." +
                    "NOTE: This will only apply to newly created spawners.")
            .define("allowSpawnedPokemonToBeCatchable", false);

    public static final ModConfigSpec.BooleanValue ALLOW_SPAWNED_POKEMON_TO_BE_DEFEATED_OUTSIDE_OF_BATTLE = BUILDER
            .comment("This allows Spawned Pokemon to be defeated outside of battle. If set to true, regardless of spawner settings, Pokemon will be required to be defeated in battle." +
                    "If set to false whether Spawned Pokemon can be defeated in battle or not is left up to the Spawner's Settings." +
                    "NOTE: This will only apply to newly created spawners.")
            .define("allowSpawnedPokemonToBeDefeatedInBattle", false);

    static final ModConfigSpec SPEC = BUILDER.build();

}
