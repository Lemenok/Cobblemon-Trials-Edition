package com.lemenok.cobblemontrialsedition.mixin;

import com.lemenok.cobblemontrialsedition.processor.StructureSpawnerReplacementProcessor;
import com.lemenok.cobblemontrialsedition.threads.ActiveStructureTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {


    @Inject(method = "placeInChunk", at = @At("HEAD"))
    private void onPlaceInChunkStart(WorldGenLevel worldGenLevel, StructureManager structureManager,
                                     ChunkGenerator chunkGenerator, RandomSource randomSource,
                                     BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo callbackInfo) {

        // Get the Id of the current Structure being generated.
        Structure structure = ((StructureStart) (Object) this).getStructure();
        ResourceLocation structureId = worldGenLevel.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getKey(structure);

        ActiveStructureTracker.set(structureId);
    }

    @Inject(method = "placeInChunk", at = @At("RETURN"))
    private void onPlaceInChunkEnd(WorldGenLevel worldGenLevel, StructureManager structureManager,
                                   ChunkGenerator chunkGenerator, RandomSource randomSource,
                                   BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo callbackInfo) {

        // TODO: Change to check for blocks that should be replaced.
        // Catch any raw/hardcoded spawners placed directly in this chunk's structure piece bounds (e.g., Mineshafts)
        BlockPos.betweenClosedStream(boundingBox).forEach(pos -> {
            if (worldGenLevel.getBlockState(pos).is(Blocks.SPAWNER)) {
                ResourceLocation structureId = ActiveStructureTracker.get();
                StructureSpawnerReplacementProcessor.processSpawner(worldGenLevel, pos.immutable(), structureId);
            }
        });

        // Clean up the thread.
        ActiveStructureTracker.clear();
    }
}
