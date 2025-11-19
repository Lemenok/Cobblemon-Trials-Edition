package com.lemenok.cobblemontrialsedition.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StructureProperties(
    ResourceLocation structureId,
    List<SpawnerProperties> spawnerProperties
)
{
    public static final Codec<StructureProperties> CODEC = RecordCodecBuilder.create(structure -> structure.group(
            ResourceLocation.CODEC.fieldOf("structureId").forGetter(StructureProperties::structureId),
            Codec.list(SpawnerProperties.CODEC).fieldOf("spawnerProperties").forGetter(StructureProperties::spawnerProperties)

    ).apply(structure, StructureProperties::new));

    public List<SpawnerProperties> getSpawnerPropertiesIfResourceLocationMatches(ResourceLocation resourceLocation){
        return structureId.equals(resourceLocation) ? spawnerProperties : null;
    }
}
