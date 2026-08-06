package com.lemenok.cobblemontrialsedition.mixin;

import com.lemenok.cobblemontrialsedition.processor.StructureSpawnerReplacementProcessor;
import com.lemenok.cobblemontrialsedition.threads.ActiveStructureTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SculkPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkPatchFeature.class)
public class SculkPatchFeatureMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void onPlaceSculkPatch(FeaturePlaceContext<SculkPatchConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // Check if the Sculk Patch successfully generated.
        if (callbackInfoReturnable.getReturnValue()) {
            WorldGenLevel level = featurePlaceContext.level();
            BlockPos origin = featurePlaceContext.origin();

            // 15 blocks safely covers the maximum spread distance of standard worldgen sculk patches.
            int radius = 15;
            BlockPos min = origin.offset(-radius, -radius, -radius);
            BlockPos max = origin.offset(radius, radius, radius);

            ResourceLocation structureId = ResourceLocation.withDefaultNamespace("sculk_patch");

            // Direct check against the StructureManager to accurately detect the Ancient City
            if (level.getLevel() instanceof ServerLevel serverLevel) {
                Structure ancientCity = serverLevel.registryAccess()
                        .registryOrThrow(Registries.STRUCTURE)
                        .get(ResourceLocation.withDefaultNamespace("ancient_city"));

                // If the origin of the sculk patch is inside an Ancient City bounding box, update the ID
                if (ancientCity != null && serverLevel.structureManager().getStructureAt(origin, ancientCity).isValid()) {
                    structureId = ResourceLocation.withDefaultNamespace("ancient_city");
                }
            }

            ResourceLocation finalStructureId = structureId;
            BlockPos.betweenClosedStream(min, max).forEach(pos -> {
                if (level.getBlockState(pos).is(Blocks.SCULK_SHRIEKER)) {

                    // Route the found block to your processor.
                    StructureSpawnerReplacementProcessor.processSpawner(level, pos.immutable(), finalStructureId);
                }
            });
        }
    }
}
