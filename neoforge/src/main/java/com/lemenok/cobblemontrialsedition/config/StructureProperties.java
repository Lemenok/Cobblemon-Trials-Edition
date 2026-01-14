package com.lemenok.cobblemontrialsedition.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public record StructureProperties(
    HolderSet<Structure> structureId,
    List<SpawnerProperties> spawnerProperties
)
{
    public static final Codec<StructureProperties> CODEC = RecordCodecBuilder.create(structure -> structure.group(
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("structureId").forGetter(StructureProperties::structureId),
            Codec.list(SpawnerProperties.CODEC).fieldOf("spawnerProperties").forGetter(StructureProperties::spawnerProperties)
    ).apply(structure, StructureProperties::new));

    public List<SpawnerProperties> getSpawnerPropertiesIfResourceLocationMatches(Holder<Structure> structureHolder){
        return structureId.contains(structureHolder) ? spawnerProperties : null;
    }
}
