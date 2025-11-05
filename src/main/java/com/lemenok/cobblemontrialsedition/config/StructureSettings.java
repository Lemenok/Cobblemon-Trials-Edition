package com.lemenok.cobblemontrialsedition.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class StructureSettings {
    private final HashMap<String, List<SpawnerSettings>> StructureSpawnerMapping = new HashMap<>();

    public boolean DoesStructureHaveSpawners(String structureId){
        return !StructureSpawnerMapping.get(structureId).isEmpty();
    }

    public void AddToStructureSpawnerMapping(String structureId, List<SpawnerSettings> spawnerSettingsList) {
        StructureSpawnerMapping.put(structureId, spawnerSettingsList);
    }

    public List<SpawnerSettings> GetSpawnersByStructureId(String stuctureId){
        return StructureSpawnerMapping.get(stuctureId);
    }

    // Get the Spawner settings that match the structure and Entity we want to replace.
    public @Nullable SpawnerSettings GetSpawnerSettingsByStructureIdAndSpawnerEntityToReplace(String structureId, String spawnerEntityToReplace){
        List<SpawnerSettings> spawnerSettingsList = this.GetSpawnersByStructureId(structureId);

        String[] splitString = spawnerEntityToReplace.split(":");

        ResourceLocation entityResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);
        EntityType entityTypeToReplace = BuiltInRegistries.ENTITY_TYPE.getOptional(entityResourceLocation)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Entity: " + spawnerEntityToReplace));

        // Iterate through the list of CobblemonSpawners for this structure and return one that matches the entityType
        // we want to replace.
        for(SpawnerSettings spawnerSettings: spawnerSettingsList){
            if(spawnerSettings.DoesSpawnerSettingsContainEntityToReplace(entityTypeToReplace))
                return spawnerSettings;
        }

        // We return null to indicate we have no replacement for that EntityType spawner.
        return null;
    }
}
