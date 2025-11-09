package com.lemenok.cobblemontrialsedition.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class StructureSettings {
    private final HashMap<ResourceLocation, List<SpawnerSettings>> StructureSpawnerMapping = new HashMap<>();

    public boolean DoesStructureHaveSpawners(ResourceLocation structureResource){
        var structureSpawnerMapping = StructureSpawnerMapping.get(structureResource);

        return structureSpawnerMapping != null;
    }

    public void AddToStructureSpawnerMapping(ResourceLocation structureResource, List<SpawnerSettings> spawnerSettingsList) {
        StructureSpawnerMapping.put(structureResource, spawnerSettingsList);
    }

    public List<SpawnerSettings> GetSpawnersByResourceLocation(ResourceLocation structureResource){
        return StructureSpawnerMapping.get(structureResource);
    }

    // Get the Spawner settings that match the structure and Entity we want to replace.
    public @Nullable SpawnerSettings GetSpawnerSettingsByStructureIdAndSpawnerEntityToReplace(ResourceLocation structureResource, EntityType spawnerEntityToReplace){
        List<SpawnerSettings> spawnerSettingsList = this.GetSpawnersByResourceLocation(structureResource);

        /*
        String[] splitString = spawnerEntityToReplace.split(":");

        ResourceLocation entityResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);
        EntityType entityTypeToReplace = BuiltInRegistries.ENTITY_TYPE.getOptional(entityResourceLocation)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Entity: " + spawnerEntityToReplace));*/

        // Iterate through the list of CobblemonSpawners for this structure and return one that matches the entityType
        // we want to replace.
        for(SpawnerSettings spawnerSettings: spawnerSettingsList){
            if(spawnerSettings.DoesSpawnerSettingsContainEntityToReplace(spawnerEntityToReplace))
                return spawnerSettings;
        }

        // We return null to indicate we have no replacement for that EntityType spawner.
        return null;
    }
}
