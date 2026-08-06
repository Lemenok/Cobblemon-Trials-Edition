package com.lemenok.cobblemontrialsedition.caches;

import net.minecraft.resources.ResourceLocation;

public record SpawnerCacheKey (
        ResourceLocation structureId,
        ResourceLocation blockEntityType,
        ResourceLocation entityType
)
{}

