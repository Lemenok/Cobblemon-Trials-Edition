package com.lemenok.cobblemontrialsedition;

import me.shedaniel.autoconfig.ConfigData;

import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@me.shedaniel.autoconfig.annotation.Config(name = CobblemonTrialsEditionFabric.MODID)
public class Config implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to log data regarding the location and data stored when placing cobblemon trial spawners.")
    public boolean ENABLE_DEBUG_LOGS = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to do any spawner replacement at all. If set to false, this will disable all spawner replacement.")
    public boolean REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to do any spawner replacement in structures. If set to false, this will disable all spawner replacement in structures.")
    public boolean REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to do any spawner replacement in Features. If set to false, this will disable the spawner replacement for any spawners in features (eg. Monster_Rooms)")
    public boolean REPLACE_SPAWNERS_IN_FEATURES = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether to replace any spawners that are not listed in configuration with a default spawner. If set to false, this will leave spawners that do not have custom spawn data created.")
    public boolean REPLACE_ANY_UNSPECIFIED_SPAWNERS_WITH_DEFAULT_COBBLEMON_SPAWNERS = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be Catchable. If set to True, regardless of spawner settings, Pokemon will always be catchable.\" +\n" +
            "\"If set to false whether Spawned Pokemon can be catchable or not is left up to the Spawner's Settings.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean ALLOW_SPAWNED_POKEMON_TO_BE_CATCHABLE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be defeated outside of battle. If set to true, regardless of spawner settings, Pokemon will be required to be defeated in battle.\" +\n" +
            "\"If set to false whether Spawned Pokemon can be defeated in battle or not is left up to the Spawner's Settings.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean ALLOW_SPAWNED_POKEMON_TO_BE_DEFEATED_OUTSIDE_OF_BATTLE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Spawned Pokemon to be Aggressive and attack players when spawned.\" +\n" +
            "\"If set to true whether Spawned Pokemon can be aggressive or not is left up to the Pokemon's Settings.\" +\n" +
            "\"NOTE: This will only apply to newly created spawners.")
    public boolean ALLOW_SPAWNED_POKEMON_TO_BE_AGGRESSIVE = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Percentage based control of mob spawner replacement.\" +\n" +
            "\"If set to true this will use the Mob Spawner Replacement Percentage to determine how often a mob spawner should be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public boolean REPLACE_MOB_SPAWNERS_BASED_ON_PERCENTAGE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is what percentage (0.0 - 1.0) Mob Spawners will be replaced, eg: .75 will mean 75% of the mob spawners on average will be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public double MOB_SPAWNER_REPLACEMENT_PERCENTAGE = 0.0;

    @ConfigEntry.Gui.Tooltip
    @Comment("This allows Percentage based control of trial spawner replacement.\" +\n" +
            "\"If set to true this will use the Trial Spawner Replacement Percentage to determine how often a trial spawner should be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public boolean REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE = false;

    @ConfigEntry.Gui.Tooltip
    @Comment("This is what percentage (0.0 - 1.0) Trial Spawners will be replaced, eg: .75 will mean 75% of the trial spawners on average will be replaced.\" +\n" +
            "\"NOTE: This will only apply in newly generated chunks.")
    public double TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE = 0.0;
}

