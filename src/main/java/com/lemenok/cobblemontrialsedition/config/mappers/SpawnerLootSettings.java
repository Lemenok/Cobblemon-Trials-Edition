package com.lemenok.cobblemontrialsedition.config.mappers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnerLootSettings {
    private static final Gson GSON = new GsonBuilder().create();

    /*
    private static class LootEntry {
        String id;
        Integer weight;
    }

    public static SimpleWeightedRandomList<ResourceKey<LootTable>> loadFromConfigFolder(Path configDirectory){
        Map<String, Integer> weights = new HashMap<>();

        // Check if there are files to load, if there are no files, return empty list.
        if (Files.notExists(configDirectory) || !Files.isDirectory(configDirectory)) {
            return new SimpleWeightedRandomList.Builder<ResourceKey<LootTable>>().build();
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(configDirectory, "*.json")) {
            for (Path file: directoryStream){
                try{
                    String fileContent = Files.readString(file);
                    processFileContent(fileContent, weights);
                } catch (IOException | JsonParseException e) {
                    // todo: add logger settings
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> builder = new SimpleWeightedRandomList.Builder<>();
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            String id = entry.getKey();
            int weight = Math.max(0, entry.getValue());
            if (weight <= 0) continue; // skip zero weight entries
            ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
            if (resourceLocation == null) {
                System.err.println("Invalid resource id in configs: " + id);
                continue;
            }
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation);
            builder.add(key, weight);
        }

        return builder.build();
    }

    private static void processFileContent(String fileContent, Map<String, Integer> weights) {
        JsonElement jsonElementRoot = JsonParser.parseString(fileContent);

        List<LootEntry> lootEntryList = new ArrayList<>();

        if(jsonElementRoot.isJsonArray()) {
            Type listType = new TypeToken<List<LootEntry>>() {}.getType();
            lootEntryList = GSON.fromJson(jsonElementRoot, listType);
        } else if (jsonElementRoot.isJsonObject()) {
            JsonObject jsonObject = jsonElementRoot.getAsJsonObject();
            if(jsonObject.has("loot") && jsonObject.get("loot").isJsonArray()) {
                Type listType = new TypeToken<List<LootEntry>>() {}.getType();
                lootEntryList = GSON.fromJson(jsonObject.get("loot"), listType);
            } else {
                // If object looks like a single entry
                LootEntry singleEntry = GSON.fromJson(jsonObject, LootEntry.class);
                if (singleEntry != null && singleEntry.id != null) lootEntryList.add(singleEntry);
            }
        } else {
            throw new JsonSyntaxException("Unexpected Json root type for the loot Config");
        }

        for (LootEntry entry: lootEntryList) {
            if (entry == null || entry.id == null) continue;
            int weight = (entry.weight == null) ? 1 : entry.weight;
            if (weight < 0) weight = 0;
            weights.merge(entry.id, weight, Integer::sum);
        }
    } */

    public void registerConfigLootTable(String namespace, String path, LootTable newLootTable) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, path);
        ResourceKey<LootTable> lootTableResourceKey = ResourceKey.create(Registries.LOOT_TABLE, resourceLocation);

        Registry.register(BuiltInRegistries.REGISTRY.get(Registries.LOOT_TABLE.registry()), resourceLocation, newLootTable);
    }
}
