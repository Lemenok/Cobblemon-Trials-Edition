package com.lemenok.cobblemontrialsedition.mixin;

import com.lemenok.cobblemontrialsedition.processor.SpawnerReplacementHelper;
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
    private void onPlaceDungeon(FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        // If the dungeon successfully generated
        if (cir.getReturnValue()) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin(); // The spawner is placed at the exact origin pos

            // Verify the spawner is actually there (it should be)
            if (level.getBlockState(origin).is(net.minecraft.world.level.block.Blocks.SPAWNER)) {
                SpawnerReplacementHelper.processLiveSpawner(level, origin, ResourceLocation.withDefaultNamespace("monster_room"));
            }
        }
    }
}
