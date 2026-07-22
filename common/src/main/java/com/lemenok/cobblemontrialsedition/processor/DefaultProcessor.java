package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DefaultProcessor implements IBlockProcessor{

    private ResourceLocation ENTITY_ID;
    private ResourceLocation STRUCTURE_ID;
    private final ResourceLocation BLOCK;

    private SpawnerProperties SPAWNERPROPERTY;

    public DefaultProcessor(ResourceLocation block) {
        BLOCK = block;
    }

    @Override
    public void setStructureId(ResourceLocation structureId) {
        STRUCTURE_ID = structureId;
    }

    @Override
    public ResourceLocation getStructureId() {
        return STRUCTURE_ID;
    }

    @Override
    public void setEntityid(CompoundTag nbt) {
        if (nbt.contains("SpawnData", 10)) {
            CompoundTag entityData = nbt.getCompound("SpawnData").getCompound("entity");
            ENTITY_ID = ResourceLocation.parse(entityData.getString("id"));
        }
    }

    @Override
    public ResourceLocation getEntityId() {
        return ENTITY_ID;
    }

    @Override
    public ResourceLocation getBlock() {
        return BLOCK;
    }

    @Override
    public boolean shouldBlockBeReplaced() {
        // TODO: Change from Shriekers to Random block Percentage
        if(Services.PLATFORM.getCommonConfig().REPLACE_SHRIEKERS_BASED_ON_PERCENTAGE){
            return Services.PLATFORM.getCommonConfig().SHRIEKER_REPLACEMENT_PERCENTAGE <= Math.random();
        }
        return false;
    }

    @Override
    public boolean doesConfigurationExistForReplacement(CacheType cacheType) {
        SPAWNERPROPERTY = PropertiesCache.getSpawnerPropertiesFromLocationEntityBlock(STRUCTURE_ID, BLOCK, ENTITY_ID, cacheType);

        return SPAWNERPROPERTY != null;
    }

    @Override
    public StructureTemplate.StructureBlockInfo buildCobblemonTrialSpawnerBlock() {
        return null;
    }
}
