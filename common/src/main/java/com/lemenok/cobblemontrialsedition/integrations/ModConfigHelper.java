package com.lemenok.cobblemontrialsedition.integrations;

import com.lemenok.cobblemontrialsedition.platform.Services;
import com.lemenok.cobblemontrialsedition.builder.IBlockBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class ModConfigHelper {

    private static final Logger LOGGER = LogManager.getLogger(Services.PLATFORM.getModID());

    public static boolean isStructureBlacklisted(LevelReader level, IBlockBuilder blockProcessor) {

        ResourceLocation targetId = blockProcessor.getStructureId();
        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, blockProcessor.getStructureId());

        Optional<Holder.Reference<Structure>> optionalStructureHolder = structureRegistry.getHolder(structureKey);

        List<String> blacklistedItems = (List<String>) Services.PLATFORM.getCommonConfig().BLACKLISTED_STRUCTURE_IDS;

        for (String entry : blacklistedItems) {
            if (entry.startsWith("#")) {
                if (optionalStructureHolder.isPresent()) {
                    ResourceLocation tagId = ResourceLocation.parse(entry.substring(1));
                    TagKey<Structure> structureTag = TagKey.create(Registries.STRUCTURE, tagId);

                    if (optionalStructureHolder.get().is(structureTag)) {
                        LOGGER.info("Structure Tag is Blacklisted: #{}:{}", tagId.getNamespace(), tagId.getPath());
                        return true;
                    }
                }
            } else {
                ResourceLocation structureId = ResourceLocation.parse(entry);

                if (targetId.equals(structureId)) {
                    LOGGER.info("Structure is Blacklisted: {}:{}", structureId.getNamespace(),structureId.getPath());
                    return true;
                }
            }
        }

        return false;
    }
}
