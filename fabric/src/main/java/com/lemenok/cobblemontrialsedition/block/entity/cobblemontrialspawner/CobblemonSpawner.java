package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CobblemonSpawner {
    void setEntityId(EntityType<?> arg, RandomSource arg2);

    static void appendHoverText(ItemStack arg, List<Component> list, String string) {
        Component component = getSpawnEntityDisplayName(arg, string);
        if (component != null) {
            list.add(component);
        }
    }

    @Nullable
    static Component getSpawnEntityDisplayName(ItemStack itemStack, String string) {
        CompoundTag compoundTag = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        ResourceLocation resourceLocation = getEntityKey(compoundTag, string);
        return resourceLocation != null ? BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation)
                .map((entityType) -> Component.translatable(entityType.getDescriptionId())
                        .withStyle(ChatFormatting.GRAY)).orElse(null) : null;
    }

    @Nullable
    private static ResourceLocation getEntityKey(CompoundTag arg, String string) {
        if (arg.contains(string, 10)) {
            String string2 = arg.getCompound(string).getCompound("entity").getString("id");
            return ResourceLocation.tryParse(string2);
        } else {
            return null;
        }
    }
}
