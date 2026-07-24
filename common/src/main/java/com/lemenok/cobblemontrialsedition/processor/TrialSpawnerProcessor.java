package com.lemenok.cobblemontrialsedition.processor;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.caches.CacheType;
import com.lemenok.cobblemontrialsedition.caches.PropertiesCache;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class TrialSpawnerProcessor implements IBlockProcessor{

    private ResourceLocation ENTITY_ID;
    private ResourceLocation STRUCTURE_ID;
    private final StructureTemplate.StructureBlockInfo STRUCTURE_BLOCK_INFO;
    private final BlockPos BLOCK_POSITION;

    private SpawnerProperties SPAWNERPROPERTY;

    public TrialSpawnerProcessor(StructureTemplate.StructureBlockInfo blockInfo) {
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
        if(Services.PLATFORM.getCommonConfig().REPLACE_TRIAL_SPAWNERS_BASED_ON_PERCENTAGE){
            return Services.PLATFORM.getCommonConfig().TRIAL_SPAWNER_REPLACEMENT_PERCENTAGE <= Math.random();
        }
        return true;
    }

    @Override
    public boolean doesConfigurationExistForReplacement(CacheType cacheType) {
        SPAWNERPROPERTY = PropertiesCache.getSpawnerPropertiesFromLocationEntityBlock(STRUCTURE_ID, getBlock(), ENTITY_ID, cacheType);

        return SPAWNERPROPERTY != null;
    }

    @Override
    public StructureTemplate.StructureBlockInfo buildCobblemonTrialSpawnerBlock(RegistryAccess registryAccess) {
        BlockState newBlockState = Services.PLATFORM.getCobblemonTrialSpawnerBlock().defaultBlockState();
        CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = BuildSpawner.create(registryAccess, BLOCK_POSITION, this);

        // Serialize the fully configured BlockEntity into an NBT tag
        CompoundTag newNbt = cobblemonTrialSpawnerEntity.saveWithFullMetadata(registryAccess);

        return new StructureTemplate.StructureBlockInfo(getBlockPosition(), newBlockState, newNbt);
    }
}
