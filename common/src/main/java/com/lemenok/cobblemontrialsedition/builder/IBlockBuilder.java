package com.lemenok.cobblemontrialsedition.builder;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface IBlockBuilder {
    public void setStructureId(ResourceLocation structureId);
    public ResourceLocation getStructureId();
    public void setEntityid(CompoundTag nbt);
    public ResourceLocation getEntityId();
    public ResourceLocation getBlock();
    public BlockPos getBlockPosition();
    public SpawnerProperties getSpawnerProperties();
    public StructureTemplate.StructureBlockInfo getStructureBlockInfo();

    public boolean shouldBlockBeReplaced();
    public boolean doesConfigurationExistForReplacement(CacheType cacheType);
    public CobblemonTrialSpawnerEntity buildCobblemonTrialSpawnerBlock(RegistryAccess registryAccess);


}
