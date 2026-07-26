package com.lemenok.cobblemontrialsedition.caches;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.Map;

public class PropertiesCache {

    private static final Map<SpawnerCacheKey, SpawnerProperties> STRUCTURES_CACHE = new HashMap<>();
    private static final Map<SpawnerCacheKey, SpawnerProperties> FEATURES_CACHE = new HashMap<>();
    private static final Map<SpawnerCacheKey, SpawnerProperties> DEFAULTS_CACHE = new HashMap<>();

    public static void rebuild(RegistryAccess registryAccess) {
        STRUCTURES_CACHE.clear();
        FEATURES_CACHE.clear();
        DEFAULTS_CACHE.clear();

        registryAccess.registry(Services.PLATFORM.getCobblemonTrialsStructureRegistry())
                .ifPresent(registry -> populateCacheFromRegistry(registry, STRUCTURES_CACHE));

        registryAccess.registry(Services.PLATFORM.getCobblemonTrialsFeaturesRegistry())
                .ifPresent(registry -> populateCacheFromRegistry(registry, FEATURES_CACHE));

        registryAccess.registry(Services.PLATFORM.getCobblemonTrialsDefaultStructureRegistry())
                .ifPresent(registry -> populateCacheFromRegistry(registry, DEFAULTS_CACHE));
    }

    private static void populateCacheFromRegistry(Registry<StructureProperties> registry, Map<SpawnerCacheKey, SpawnerProperties> targetCache) {
        // Iterate over each Structure JSON file in the registry.
        for (var entry : registry.entrySet()) {

            StructureProperties structureProperties = entry.getValue();

            String[] structureString = structureProperties.structureId().split(":");

            ResourceLocation structureResourceLocation = ResourceLocation.fromNamespaceAndPath(structureString[0], structureString[1]);

            if (structureResourceLocation == null) continue;

            // Iterate through each spawner property tied to the structure.
            for (SpawnerProperties spawnerProperties : structureProperties.spawnerProperties()) {

                // Iterate through each Block/Entity mapping to create the key and data for the hash.
                for (ResourceLocation blockType : spawnerProperties.blockEntityTypesToReplace()) {
                    for (ResourceLocation entity : spawnerProperties.mobEntitiesInSpawnerToReplace()) {

                        // Store key in Hash with the SpawnerProperties.
                        SpawnerCacheKey key = new SpawnerCacheKey(structureResourceLocation, blockType, entity);
                        targetCache.put(key, spawnerProperties);
                    }
                }
            }
        }
    }

    public static SpawnerProperties getStructureProperty(ResourceLocation structure, ResourceLocation blockEntity, ResourceLocation entity) {
        return STRUCTURES_CACHE.get(new SpawnerCacheKey(structure, blockEntity, entity));
    }

    public static SpawnerProperties getFeatureProperty(ResourceLocation featureOrStructure, ResourceLocation blockEntity, ResourceLocation entity) {
        return FEATURES_CACHE.get(new SpawnerCacheKey(featureOrStructure, blockEntity, entity));
    }

    public static SpawnerProperties getDefaultProperty(ResourceLocation structure, ResourceLocation blockEntity, ResourceLocation entity) {
        return DEFAULTS_CACHE.get(new SpawnerCacheKey(structure, blockEntity, entity));
    }

    public static SpawnerProperties getSpawnerPropertiesFromLocationEntityBlock(ResourceLocation location, ResourceLocation blockEntity, ResourceLocation entity, CacheType cacheType) {

        SpawnerProperties result = null;

        if(cacheType == CacheType.STRUCTURE)
            result = getStructureProperty(location, blockEntity, entity);

        if(cacheType == CacheType.FEATURE)
            result = getFeatureProperty(location, blockEntity, entity);

        if (result != null)
            return result;

        return getDefaultProperty(location, blockEntity, entity);
    }
}
