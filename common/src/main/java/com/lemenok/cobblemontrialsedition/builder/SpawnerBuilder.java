package com.lemenok.cobblemontrialsedition.builder;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

public class SpawnerBuilder implements IBlockBuilder {

    private ResourceLocation ENTITY_ID;
    private ResourceLocation STRUCTURE_ID;
    private final StructureTemplate.StructureBlockInfo STRUCTURE_BLOCK_INFO;
    private final LootTable LOOT_TABLE = null;
    private final BlockPos BLOCK_POSITION;

    private SpawnerProperties SPAWNERPROPERTY = null;

    public SpawnerBuilder(StructureTemplate.StructureBlockInfo blockInfo) {
        STRUCTURE_BLOCK_INFO = blockInfo;
        BLOCK_POSITION = blockInfo.pos();
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
        return BuiltInRegistries.BLOCK.getKey(STRUCTURE_BLOCK_INFO.state().getBlock());
    }

    @Override
    public BlockPos getBlockPosition() {
        return BLOCK_POSITION;
    }

    @Override
    public SpawnerProperties getSpawnerProperties() {
        return SPAWNERPROPERTY;
    }

    @Override
    public StructureTemplate.StructureBlockInfo getStructureBlockInfo() {
        return STRUCTURE_BLOCK_INFO;
    }

    @Override
    public boolean shouldBlockBeReplaced() {
        if(Services.PLATFORM.getCommonConfig().REPLACE_MOB_SPAWNERS_BASED_ON_PERCENTAGE){
            return Services.PLATFORM.getCommonConfig().MOB_SPAWNER_REPLACEMENT_PERCENTAGE <= Math.random();
        }
        return true;
    }

    @Override
    public boolean doesConfigurationExistForReplacement(CacheType cacheType) {
        SPAWNERPROPERTY = PropertiesCache.getSpawnerPropertiesFromLocationEntityBlock(STRUCTURE_ID, getBlock(), ENTITY_ID, cacheType);

        return SPAWNERPROPERTY != null;
    }

    @Override
    public CobblemonTrialSpawnerEntity buildCobblemonTrialSpawnerBlock(RegistryAccess registryAccess) {
        return BuildSpawner.create(registryAccess, BLOCK_POSITION, this);
    }
}
