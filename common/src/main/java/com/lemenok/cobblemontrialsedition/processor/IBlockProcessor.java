package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.caches.CacheType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public interface IBlockProcessor {
    public void setStructureId(ResourceLocation structureId);
    public ResourceLocation getStructureId();
    public void setEntityid(CompoundTag nbt);
    public ResourceLocation getEntityId();
    public ResourceLocation getBlock();

    public boolean shouldBlockBeReplaced();
    public boolean doesConfigurationExistForReplacement(CacheType cacheType);
    public StructureTemplate.StructureBlockInfo buildCobblemonTrialSpawnerBlock();

}
