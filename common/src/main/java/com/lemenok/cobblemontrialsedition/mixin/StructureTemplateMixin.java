package com.lemenok.cobblemontrialsedition.mixin;

import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.processor.SpawnerReplacementProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    @Inject(
            method = "placeInWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Lnet/minecraft/util/RandomSource;I)Z",
            at = @At("HEAD")
    )
    private void injectCustomProcessors(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        // Add our processor to the settings before the structure starts placing blocks.
        // Using addProcessor ensures it gets tacked onto whatever datapack processors are already running.
        settings.addProcessor(new SpawnerReplacementProcessor());
    }

    /*
    @Inject(
            method = "processBlockInfos",
            at = @At("HEAD"),
            require = 1
    )
    private static void safelyInjectProcessor(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureTemplate.StructureBlockInfo> blockInfos,
            StructureTemplate template,
            CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir
    ) {
        // Access the processors from the settings object
        List<net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor> processors = settings.getProcessors();

        // Add only if not present
        if (processors.stream().noneMatch(p -> p instanceof SpawnerReplacementProcessor)) {
            settings.addProcessor(new SpawnerReplacementProcessor());
        }
    }*/

    /*
    @Inject(method = "placeInWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;"
            ), require = 1)
    private void injectGlobalSpawnerProcessor(ServerLevelAccessor level, BlockPos offset, BlockPos pos,
                                              StructurePlaceSettings settings, RandomSource random,
                                              int flags, CallbackInfoReturnable<Boolean> cir)
    {
        boolean alreadyAdded = settings.getProcessors().stream()
                .anyMatch(p -> p instanceof SpawnerReplacementProcessor);

        if (!alreadyAdded) {
            settings.addProcessor(new SpawnerReplacementProcessor());
            System.out.println("Injected processor into active structure template!");
        }


        //LOGGER.info("Entered Structure Mixin");

        //settings.addProcessor(new SpawnerReplacementProcessor());
    }
    */

    /*
    @Inject(method = "processBlockInfos", at = @At("HEAD"))
    private static void injectGlobalSpawnerProcessor(
            ServerLevelAccessor level,
            BlockPos offset,
            BlockPos pos,
            StructurePlaceSettings settings,
            List<StructureTemplate.StructureBlockInfo> blockInfos,
            CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir
    ) {
        // 1. Ensure we don't infinitely stack processors if settings are reused
        boolean alreadyAdded = settings.getProcessors().stream()
                .anyMatch(p -> p instanceof SpawnerReplacementProcessor);

        if (!alreadyAdded) {
            settings.addProcessor(new SpawnerReplacementProcessor());
        }

        // 2. The Truth Teller Log:
        System.out.println("[DEBUG] processBlockInfos hit! Blocks in this chunk: " + blockInfos.size() + " | Total Processors: " + settings.getProcessors().size());
    }*/
}
