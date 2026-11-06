package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.resources.ResourceLocation;

public class WeightedLootEntry {
    private ResourceLocation location;
    private int weight;

    public WeightedLootEntry(ResourceLocation location, int weight) {
        this.location = location;
        this.weight = weight;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
