package com.lemenok.cobblemontrialsedition.threads;

import net.minecraft.resources.ResourceLocation;

public class ActiveStructureTracker {
    private static final ThreadLocal<ResourceLocation> CURRENT_STRUCTURE = new ThreadLocal<>();

    public static void set(ResourceLocation id) {
        CURRENT_STRUCTURE.set(id);
    }

    public static void clear() {
        CURRENT_STRUCTURE.remove();
    }

    public static ResourceLocation get() {
        return CURRENT_STRUCTURE.get();
    }
}
