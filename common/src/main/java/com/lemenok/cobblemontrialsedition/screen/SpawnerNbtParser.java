package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawner;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public class SpawnerNbtParser {

    /**
     * Converts the BlockEntity's raw CompoundTag into your SpawnerProperties record.
     */
    public static SpawnerProperties parse(CobblemonTrialSpawner cobblemonTrialSpawner, CompoundTag rootTag) {
        var normalConfig = cobblemonTrialSpawner.getConfig();
        //var ominousConfig = cobblemonTrialSpawner.getOminousConfig();
        var normalSpawnData = rootTag.getCompound("normal_config").getList("spawn_potentials", Tag.TAG_COMPOUND);
        var ominousSpawnData = rootTag.getCompound("ominous_config").getList("spawn_potentials", Tag.TAG_COMPOUND);
        var normalLootTablesToEject = rootTag.getCompound("normal_config").getList("loot_tables_to_eject", Tag.TAG_COMPOUND);
        var ominousLootTablesToEject = rootTag.getCompound("ominous_config").getList("loot_tables_to_eject", Tag.TAG_COMPOUND);

        // Parse Roster Lists
        List<SpawnablePokemonProperties> normalRoster = parsePokemonRoster(normalSpawnData);
        List<SpawnablePokemonProperties> ominousRoster = parsePokemonRoster(ominousSpawnData);

        // Parse Loot Tables (Assuming they are stored as a list of string paths in the NBT)
        SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables = parseLootTables(normalLootTablesToEject);
        SimpleWeightedRandomList<ResourceKey<LootTable>> ominousLootTables = parseLootTables(ominousLootTablesToEject);

        return new SpawnerProperties(
                new ArrayList<>(), // blocks to replace
                new ArrayList<>(), // entities to replace
                normalConfig.ticksBetweenSpawn(),
                cobblemonTrialSpawner.getTargetCooldownLength(),
                cobblemonTrialSpawner.getRequiredPlayerRange(),
                normalConfig.spawnRange(),
                (int) normalConfig.simultaneousMobs(),
                (int) normalConfig.simultaneousMobsAddedPerPlayer(),
                (int) normalConfig.totalMobs(),
                (int) normalConfig.totalMobsAddedPerPlayer(),
                lootTables,
                ominousLootTables,
                normalConfig.enableOminousSpawnerAttacks(),
                true, // glowing
                normalRoster,
                ominousRoster
        );
    }

    private static List<SpawnablePokemonProperties> parsePokemonRoster(ListTag potentialsList) {
        List<SpawnablePokemonProperties> roster = new ArrayList<>();

        for (int i = 0; i < potentialsList.size(); i++) {
            CompoundTag entryTag = potentialsList.getCompound(i);
            int weight = entryTag.getInt("weight");

            // Dig down to data -> entity -> Pokemon
            CompoundTag pokemonNbt = entryTag.getCompound("data").getCompound("entity").getCompound("Pokemon");

            // Extract core properties
            String species = pokemonNbt.getString("Species").replace("cobblemon:", "");
            int level = pokemonNbt.getInt("Level");
            String form = pokemonNbt.getString("FormId");
            boolean isShiny = pokemonNbt.getBoolean("Shiny");
            String gender = pokemonNbt.getString("Gender");
            String teraType = pokemonNbt.getString("TeraType").replace("cobblemon:", "");

            // Extract PersistentData booleans
            CompoundTag persistentData = pokemonNbt.getCompound("PersistentData");
            boolean isAggressive = persistentData.getBoolean("is_aggressive");
            boolean isUncatchable = persistentData.getBoolean("is_uncatchable");
            boolean isAlwaysAlpha = persistentData.getBoolean("is_always_alpha");

            // Build Stats
            SpawnablePokemonStats stats = new SpawnablePokemonStats(
                    List.of(form), level, gender, "", new ArrayList<>(), new ArrayList<>(),
                    "", new ArrayList<>(), "", 0, teraType, isShiny
            );

            // Build full properties
            SpawnablePokemonProperties properties = new SpawnablePokemonProperties(
                    species, weight, 1.0f, isUncatchable, false, isAggressive, isAlwaysAlpha, new ArrayList<>(), stats
            );

            roster.add(properties);
        }

        return roster;
    }

    private static SimpleWeightedRandomList<ResourceKey<LootTable>> parseLootTables(ListTag lootList) {
        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> builder = new SimpleWeightedRandomList.Builder<>();

        for (int i = 0; i < lootList.size(); i++) {
            CompoundTag lootEntry = lootList.getCompound(i);
            String tableData = lootEntry.getString("data");
            int weight = lootEntry.contains("weight") ? lootEntry.getInt("weight") : 1; // Fallback weight if not specified

            if (!tableData.isEmpty()) {
                ResourceLocation location = ResourceLocation.tryParse(tableData);
                if (location != null) {
                    ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, location);
                    builder.add(lootTableKey, weight);
                }
            }
        }

        return builder.build();
    }
}
