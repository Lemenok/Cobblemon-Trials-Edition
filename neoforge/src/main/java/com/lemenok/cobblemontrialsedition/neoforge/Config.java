package com.lemenok.cobblemontrialsedition.neoforge;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGS = BUILDER
            .comment("Whether to log data regarding the location and data stored when placing cobblemon trial spawners.")
            .define("enableDebugLogs", false);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_STRUCTURE_IDS = BUILDER
            .comment("A List of structures that will be skipped. Prefix with '#' to denote a structure tag (e.g., '#minecraft:village').")
            .defineList("blacklistedStructureIds", List::of, obj -> {
                        if (!(obj instanceof String resourceLocation)) return false;

                        String idToCheck = resourceLocation.startsWith("#") ? resourceLocation.substring(1) : resourceLocation;
                        return ResourceLocation.tryParse(idToCheck) != null;
                    });

    public static final ModConfigSpec.BooleanValue REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement at all. If set to false, this will disable all spawner replacement.")
            .define("replaceGeneratedSpawnersWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to do any spawner replacement in structures. If set to false, this will disable all spawner replacement in structures.")
            .define("replaceSpawnersInStructuresWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_SPAWNERS_IN_FEATURES = BUILDER
            .comment("Whether to do any spawner replacement in Features. If set to false, this will disable the spawner replacement for any spawners in features (eg. Monster_Rooms)")
            .define("replaceSpawnersInFeaturesWithCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = BUILDER
            .comment("Whether to replace any spawners that are not listed in configuration with a default spawner. If set to false, this will leave spawners that do not have custom spawn data created.")
            .define("replaceAnyUnspecifiedSpawnersWithDefaultCobblemonSpawners", true);

    public static final ModConfigSpec.BooleanValue SPAWNED_POKEMON_ARE_UNCATCHABLE = BUILDER
            .comment("This allows Spawned Pokemon to be Catchable. If set to True, regardless of spawner settings, Pokemon will always be catchable." +
                    "If set to false whether Spawned Pokemon can be catchable or not is left up to the Spawner's Settings." +
                    "NOTE: This will only apply to newly created spawners.")
            .define("spawnedPokemonAreUncatchable", true);

    public static final ModConfigSpec.BooleanValue SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE = BUILDER
            .comment("This allows Spawned Pokemon to be defeated outside of battle. If set to true, regardless of spawner settings, Pokemon will be required to be defeated in battle." +
                    "If set to false whether Spawned Pokemon can be defeated in battle or not is left up to the Spawner's Settings." +
                    "NOTE: This will only apply to newly created spawners.")
            .define("spawnedPokemonMustBeDefeatedInBattle", true);

    public static final ModConfigSpec.BooleanValue ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE = BUILDER
            .comment("This allows Spawned Pokemon to be Aggressive and attack players when spawned.\" +\n" +
                    "\"Whether Spawned Pokemon can be aggressive or not if Fight or Flight is installed. Both this setting and 'Attack Players Unprovoked' in Fight or Flight must be set to true.\" +\n" +
                    "\"NOTE: This will only apply to newly created spawners.")
            .define("allowSpawnedPokemonToBeAggressive", true);

    public static final ModConfigSpec.BooleanValue REPLACE_MOB_SPAWNERS_BASED_ON_PERCENTAGE = BUILDER
            .comment("This allows Percentage based control of mob spawner replacement." +
                    "If set to true this will use the Mob Spawner Replacement Percentage to determine how often a mob spawner should be replaced." +
                    "NOTE: This will only apply in newly generated chunks.")
            .define("replaceMobSpawnersBasedOnPercentage", false);

    public static final ModConfigSpec.DoubleValue MOB_SPAWNER_REPLACEMENT_PERCENTAGE = BUILDER
            .comment("This is what percentage (0.0 - 1.0) Mob Spawners will be replaced, eg: .75 will mean 75% of each trial spawner within a structure will be replaced.\" +\n" +
                    "\"NOTE: This will only apply in newly generated chunks.")
            .defineInRange("mobSpawnerReplacementPercentage", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE = BUILDER
            .comment("This allows Percentage based control of trial spawner replacement." +
                    "If set to true this will use the Trial Spawner Replacement Percentage to determine how often a trial spawner should be replaced." +
                    "NOTE: This will only apply in newly generated chunks.")
            .define("replaceTrialSpawnersBasedOnPercentage", false);

    public static final ModConfigSpec.DoubleValue TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE = BUILDER
            .comment("This is what percentage (0.0 - 1.0) Trial Spawners will be replaced, eg: .75 will mean 75% of each trial spawner within a structure will be replaced.\" +\n" +
                    "\"NOTE: This will only apply in newly generated chunks.")
            .defineInRange("trialSpawnerReplacementPercentage", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue REPLACE_BLOCKS_ON_PERCENTAGE = BUILDER
            .comment("This allows Percentage based control of specified Block replacement.\" +\n" +
                    "\"If set to true this will use the Block Replacement Percentage to determine how often a Block should be replaced.\" +\n" +
                    "\"NOTE: This will only apply in newly generated chunks.")
            .define("replaceBlocksBasedOnPercentage", false);

    public static final ModConfigSpec.DoubleValue BLOCK_REPLACEMENT_PERCENTAGE = BUILDER
            .comment("This is what percentage (0.0 - 1.0) Blocks will be replaced, eg: .75 will mean 75% of each Blocks within a structure will be replaced.\" +\n" +
                    "\"NOTE: This will only apply in newly generated chunks.")
            .defineInRange("BlockReplacementPercentage", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue ENABLE_POKEMON_LEVEL_ADJUSTMENT = BUILDER
            .comment("This is if the Pokemon spawned from Cobblemon Trial Spawners will adjust their level based on the players parties around them.")
            .define("enablePokemonLevelAdjustment", true);

    public static final ModConfigSpec.ConfigValue<String> POKEMON_LEVEL_ADJUSTMENT_TYPE = BUILDER
            .comment("This will determine the type of level adjustment the pokemon will have. Valid values are AVERAGE or MEDIAN. These values are calculated from \" +\n" +
                    "\"all pokemon in each players party within range of the spawner. Default is AVERAGE, unknown values will default to AVERAGE.")
            .define("pokemonLevelAdjustmentType", "AVERAGE");

    public static final ModConfigSpec.ConfigValue<Integer> TIME_TILL_POKEMON_DESPAWN_IN_TICKS = BUILDER
            .comment("This will determine how long pokemon spawned from Trial Spawners will remain until they despawn. \" +\n" +
                    "\"The time only matters when the cobblemon are loaded.")
            .define("timeTillPokemonDespawnInTicks", 6000);

    static final ModConfigSpec SPEC = BUILDER.build();

}
