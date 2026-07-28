package com.lemenok.cobblemontrialsedition.mixin;

import com.lemenok.cobblemontrialsedition.processor.StructureSpawnerReplacementProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterRoomFeature.class)
public class MonsterRoomFeatureMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void onPlaceDungeon(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        // Check if the Monster room is successfully generated.
        if (callbackInfoReturnable.getReturnValue()) {
            WorldGenLevel level = featurePlaceContext.level();
            BlockPos origin = featurePlaceContext.origin();

            // Verify the spawner exists
            if (level.getBlockState(origin).is(net.minecraft.world.level.block.Blocks.SPAWNER)) {
                StructureSpawnerReplacementProcessor.processSpawner(level, origin, ResourceLocation.withDefaultNamespace("monster_room"));
            }
        }
    }
}
