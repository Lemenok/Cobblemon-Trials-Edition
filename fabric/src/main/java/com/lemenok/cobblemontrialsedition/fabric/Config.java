package com.lemenok.cobblemontrialsedition.fabric;

import me.shedaniel.autoconfig.ConfigData;

import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@me.shedaniel.autoconfig.annotation.Config(name = CobblemonTrialsEditionFabric.MODID)
public class Config implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to log data regarding the location and data stored when placing cobblemon trial spawners.")
    public boolean ENABLE_DEBUG_LOGS = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("A List of structures that will be skipped. Prefix with '#' to denote a structure tag (e.g., '#minecraft:village').")
    public List<String> BLACKLISTED_STRUCTURE_IDS = new ArrayList<>();

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to do any spawner replacement at all. If set to false, this will disable all spawner replacement.")
    public boolean REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to do any spawner replacement in structures. If set to false, this will disable all spawner replacement in structures.")
    public boolean REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to replace any spawners that are not listed in configuration with a default spawner. If set to false, this will leave spawners that do not have custom spawn data created.")
    public boolean REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to replace any Skulk Shriekers with Cobblemon Trial Spawners.")
    public boolean REPLACE_ANY_BLOCKS_WITH_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be Catchable. If set to True, regardless of spawner settings, Pokemon will always be catchable.\" +\n" +
            "\"If set to false whether Spawned Pokemon can be catchable or not is left up to the Spawner's Settings.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean SPAWNED_POKEMON_ARE_UNCATCHABLE = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be defeated outside of battle. If set to true, regardless of spawner settings, Pokemon will be required to be defeated in battle.\" +\n" +
            "\"If set to false whether Spawned Pokemon can be defeated in battle or not is left up to the Spawner's Settings.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean SPAWNED_POKEMON_MUST_BE_DEFEATED_IN_BATTLE = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be Aggressive and attack players when spawned.\" +\n" +
            "\"Whether Spawned Pokemon can be aggressive or not if Fight or Flight is installed. This will make pokemon attack players unprovoked.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Percentage based control of mob spawner replacement.\" +\n" +
            "\"If set to true this will use the Mob Spawner Replacement Percentage to determine how often a mob spawner should be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public boolean REPLACE_MOB_SPAWNERS_BASED_ON_PERCENTAGE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is what percentage (0.0 - 1.0) Mob Spawners will be replaced, eg: .75 will mean 75% of each trial spawner within a structure will be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public double MOB_SPAWNER_REPLACEMENT_PERCENTAGE = 0.0;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Percentage based control of trial spawner replacement.\" +\n" +
            "\"If set to true this will use the Trial Spawner Replacement Percentage to determine how often a trial spawner should be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public boolean REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is what percentage (0.0 - 1.0) Trial Spawners will be replaced, eg: .75 will mean 75% of each trial spawner within a structure will be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public double TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE = 0.0;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Percentage based control of Sculk Shrieker replacement.\" +\n" +
            "\"If set to true this will use the Trial Spawner Replacement Percentage to determine how often a Sculk Shrieker should be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public boolean REPLACE_BLOCKS_BASED_ON_PERCENTAGE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is what percentage (0.0 - 1.0) Sculk Shriekers will be replaced, eg: .75 will mean 75% of each Sculk Shriekers within a structure will be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public double BLOCK_REPLACEMENT_PERCENTAGE = 0.0;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is if the Pokemon spawned from Cobblemon Trial Spawners will adjust their level based on the players parties around them.")
    public boolean ENABLE_POKEMON_LEVEL_ADJUSTMENT = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This will determine the type of level adjustment the pokemon will have. Valid values are AVERAGE or MEDIAN. These values are calculated from \" +\n" +
            "\"all pokemon in each players party within range of the spawner. Default is AVERAGE, unknown values will default to AVERAGE.")
    public String POKEMON_LEVEL_ADJUSTMENT_TYPE = "AVERAGE";

    @ConfigEntry.Gui.Tooltip
    @Comment("This will determine how long pokemon spawned from Trial Spawners will remain until they despawn. \" +\n" +
            "\"The time only matters when the cobblemon are loaded.")
    public int TIME_TILL_POKEMON_DESPAWN_IN_TICKS = 6000;
}

