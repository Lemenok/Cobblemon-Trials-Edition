package com.lemenok.cobblemontrialsedition.events;

import com.lemenok.cobblemontrialsedition.processors.ConfigProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkGenerationHandler {
    //public static final DeferredRegister.DataComponents ITEMS = DeferredRegister.create();

    // Keep track of chunks we have already processed.
    private static final Set<Long> processedChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void register() {
        NeoForge.EVENT_BUS.register(new ChunkGenerationHandler());
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Level level = (Level) event.getLevel();

        if(level.isClientSide()) return; //We only care about server side level events
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        long chunkKey = (((long) chunk.getPos().x) << 32) ^ (chunk.getPos().z & 0xffffffffL);
        // Check if we've already processed chunk key.
        if(processedChunks.contains(chunkKey)) return;

        processedChunks.add(chunkKey);

        Map<BlockPos, BlockEntity> blockEntityMap = chunk.getBlockEntities();
        for (Map.Entry<BlockPos, BlockEntity> entry : blockEntityMap.entrySet()) {
            BlockPos blockEntityPosition = entry.getKey();
            BlockEntity blockEntity = entry.getValue();
            if (blockEntity instanceof SpawnerBlockEntity) {
                if (isStructurePresentAt(serverLevel, blockEntityPosition)) {
                    serverLevel.setBlock(blockEntityPosition, Blocks.TRIAL_SPAWNER.defaultBlockState(), 3);
                }
            }
        }
    }

    private boolean isStructurePresentAt(ServerLevel serverLevel, BlockPos blockEntityPosition) {
        // Use the servers structure manager to check the structure at the position.
        StructureManager structureManager = serverLevel.structureManager();

        var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

        for (ResourceLocation resourceLocation : ConfigProcessor.WHITELISTED_STRUCTURES) {
            try {

                for (Structure structure : allStructuresAtPosition.keySet()){
                    // Get the ResourceKey for the given Structure instance.
                    Optional<ResourceKey<StructureType<?>>> optionalKey = BuiltInRegistries.STRUCTURE_TYPE.getResourceKey(structure.type());

                    // If the Structure is in the registry, proceed with the comparison.
                    if (optionalKey.isPresent()) {
                        ResourceKey<StructureType<?>> structureKey = optionalKey.get();
                        ResourceLocation structureLocation = structureKey.location(); // Get the ResourceLocation from the key.

                 if(resourceLocation.equals(structureLocation))
                            return true;
                    }
                }
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return false;
    }
}
