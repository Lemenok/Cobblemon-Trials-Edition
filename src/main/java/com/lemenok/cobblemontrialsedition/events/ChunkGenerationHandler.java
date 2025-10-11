package com.lemenok.cobblemontrialsedition.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkGenerationHandler {

    // Keep track of chunks we have already processed.
    private static final Set<Long> processedChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Structures whitelisted in the config file.
    private static final Set<ResourceLocation> whitelistStructures = new HashSet<>();

    public static void register() {
        loadConfig();
        NeoForge.EVENT_BUS.register(new ChunkGenerationHandler());
    }

    private static void loadConfig() {
        try (InputStream in = ChunkGenerationHandler.class.getClassLoader()
                .getResourceAsStream("data/cobblemontrialsedition/config.json5")) {
            if (in == null) {
                throw new RuntimeException("Unable to find config file at data/cobblevaults/config.json5");
            }

            Scanner scanner = new Scanner(in).useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";

            if(content.contains("minecraft:fortress")) {
                whitelistStructures.add(ResourceLocation.fromNamespaceAndPath("minecraft","fortress"));
            }

        } catch (IOException e) {
            throw new RuntimeException("There was an issue trying to parse the config file: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Level level = (Level) event.getLevel();

        if(level.isClientSide()) return; //We only care about server side level events
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        // Replace with a chunk hash? seems more efficient?
        long chunkKey = (((long) chunk.getPos().x) << 32) ^ (chunk.getPos().z & 0xffffffffL);
        // Check if we've already processed chunk key.
        if(processedChunks.contains(chunkKey)) return;

        processedChunks.add(chunkKey);

        Map<BlockPos, BlockEntity> blockEntityMap = chunk.getBlockEntities();
        for (Map.Entry<BlockPos, BlockEntity> entry : blockEntityMap.entrySet()) {
            BlockPos blockEntityPosition = entry.getKey();
            BlockEntity blockEntity = entry.getValue();
            if (blockEntity instanceof SpawnerBlockEntity) {
                if (isStructurePresentAt(serverLevel, blockEntityPosition, whitelistStructures)) {
                    serverLevel.setBlock(blockEntityPosition, Blocks.TRIAL_SPAWNER.defaultBlockState(), 3);
                }
            }
        }
    }

    private boolean isStructurePresentAt(ServerLevel serverLevel, BlockPos blockEntityPosition, Set<ResourceLocation> whitelistStructures) {
        // Use the servers structure manager to check the structure at the position.
        StructureManager structureManager = serverLevel.structureManager();

        var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

        for (ResourceLocation resourceLocation : whitelistStructures) {
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
