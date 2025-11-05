package com.lemenok.cobblemontrialsedition.config;

import com.lemenok.cobblemontrialsedition.config.mappers.GlobalSettingsMapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlobalSettings extends GlobalSettingsMapper {

    private List<StructureSettings> StructureSettingsList;

    public GlobalSettings(boolean enableDebugLogs,
                          boolean replaceGeneratedSpawnersWithCobblemonSpawners,
                          boolean replaceSpawnersNotInStructuresWithCobblemonSpawners,
                          boolean replaceSpawnersInStructuresWithCobblemonSpawners){
        EnableDebugLogs = enableDebugLogs;
        ReplaceGeneratedSpawnersWithCobblemonSpawners = replaceGeneratedSpawnersWithCobblemonSpawners;
        ReplaceSpawnersNotInStructuresWithCobblemonSpawners = replaceSpawnersNotInStructuresWithCobblemonSpawners;
        ReplaceSpawnersInStructuresWithCobblemonSpawners = replaceSpawnersInStructuresWithCobblemonSpawners;
    }

    public boolean isReplaceGeneratedSpawnersWithCobblemonSpawners() {
        return ReplaceGeneratedSpawnersWithCobblemonSpawners;
    }

    public boolean isReplaceSpawnersNotInStructuresWithCobblemonSpawners() {
        return ReplaceSpawnersNotInStructuresWithCobblemonSpawners;
    }

    public boolean isReplaceSpawnersInStructuresWithCobblemonSpawners() {
        return ReplaceSpawnersInStructuresWithCobblemonSpawners;
    }

    public boolean isDebugModeEnabled(){
        return EnableDebugLogs;
    }

    public void setStructureSettingsList(List<StructureSettings> structureSettingsList){
        this.StructureSettingsList = structureSettingsList;
    }

    public void addStructureSettingsToList(StructureSettings structureSettings){
        this.StructureSettingsList.add(structureSettings);
    }

    public List<StructureSettings> getStructureSettingsList() {
        return StructureSettingsList;
    }

    public @Nullable StructureSettings getStructureSettingsByStructureId(String structureId){
        for(StructureSettings structureSettings: this.StructureSettingsList){
            if(structureSettings.DoesStructureHaveSpawners(structureId))
                return structureSettings;
        }

        return null;
    }
}
