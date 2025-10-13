package com.lemenok.cobblemontrialsedition.models;

import java.util.ArrayList;

public class ConfigRoot {
    public boolean replaceSpawnersOutsideOfStructuresWithDefaultTrailSpawner;
    public boolean enableChestReplacementWithVaults;
    public ArrayList<CustomSpawnerSetting> customSpawnerSettings;
    public ArrayList<DefaultSpawnerSetting> defaultSpawnerSettings;
    public ArrayList<VaultSetting> vaultSettings;
}
