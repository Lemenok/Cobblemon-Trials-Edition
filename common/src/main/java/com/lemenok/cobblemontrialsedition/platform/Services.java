package com.lemenok.cobblemontrialsedition.platform;

import com.lemenok.cobblemontrialsedition.integrations.IModIntegrations;

import java.util.ServiceLoader;

public class Services {
    public static final IModIntegrations PLATFORM = load(IModIntegrations.class);

    public static <T> T load(Class<T> newClass) {
        return ServiceLoader.load(newClass)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + newClass.getName()));
    }
}
