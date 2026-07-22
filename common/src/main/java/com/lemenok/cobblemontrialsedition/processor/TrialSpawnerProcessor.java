package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class TrialSpawnerProcessor implements IBlockProcessor{

    private ResourceLocation ENTITY_ID;
    private ResourceLocation STRUCTURE_ID;
    private final ResourceLocation BLOCK;

    private SpawnerProperties SPAWNERPROPERTY;

    public TrialSpawnerProcessor(ResourceLocation block) {
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
        if (nbt.contains("normal_config")) {
            CompoundTag normalConfig = nbt.getCompound("normal_config");
            if (normalConfig.contains("spawn_potentials")) {
                ListTag spawnPotentials = normalConfig.getList("spawn_potentials", ListTag.TAG_COMPOUND);
                for (int i = 0; i < spawnPotentials.size(); i++) {
                    CompoundTag entry = spawnPotentials.getCompound(i);

                    CompoundTag dataTag = entry.getCompound("data");
                    CompoundTag entityTag = dataTag.getCompound("entity");

                    ENTITY_ID = ResourceLocation.parse(entityTag.getString("id"));
                }
            }
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
        if(Services.PLATFORM.getCommonConfig().REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE){
            return Services.PLATFORM.getCommonConfig().TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE <= Math.random();
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
