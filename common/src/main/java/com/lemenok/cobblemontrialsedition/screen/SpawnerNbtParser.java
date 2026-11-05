package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawner;
import com.lemenok.cobblemontrialsedition.config.PokemonSpawnConfig;
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
     * Converts the BlockEntity's raw CompoundTag into SpawnerProperties record.
     */
    public static SpawnerProperties parse(CobblemonTrialSpawner cobblemonTrialSpawner, CompoundTag rootTag) {
        var normalConfig = cobblemonTrialSpawner.getConfig();
        //var ominousConfig = cobblemonTrialSpawner.getOminousConfig();
        var normalSpawnData = rootTag.getCompound("normal_config").getList("spawn_potentials", Tag.TAG_COMPOUND);
        var ominousSpawnData = rootTag.getCompound("ominous_config").getList("spawn_potentials", Tag.TAG_COMPOUND);
        var normalWavesData = rootTag.getCompound("normal_config").getList("listOfPokemonWaves", Tag.TAG_COMPOUND);
        var ominousWavesData = rootTag.getCompound("ominous_config").getList("listOfPokemonWaves", Tag.TAG_COMPOUND);
        var normalLootTablesToEject = rootTag.getCompound("normal_config").getList("loot_tables_to_eject", Tag.TAG_COMPOUND);
        var ominousLootTablesToEject = rootTag.getCompound("ominous_config").getList("loot_tables_to_eject", Tag.TAG_COMPOUND);
        boolean isWaveMode = rootTag.contains("is_wave_mode") ? rootTag.getBoolean("is_wave_mode") : cobblemonTrialSpawner.getConfig().isWaveMode();

        // Parse Roster Lists
        PokemonSpawnConfig pokemonSpawnConfig = new PokemonSpawnConfig(
                isWaveMode,
                parsePokemonRoster(normalSpawnData),
                parsePokemonRoster(ominousSpawnData),
                parsePokemonWavesRoster(normalWavesData),
                parsePokemonWavesRoster(ominousWavesData)
        );

        // Parse Loot Tables (Assuming they are stored as a list of string paths in the NBT)
        SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables = parseLootTables(normalLootTablesToEject);
        SimpleWeightedRandomList<ResourceKey<LootTable>> ominousLootTables = parseLootTables(ominousLootTablesToEject);

        return new SpawnerProperties(
                new ArrayList<>(), // blocks to replace
                new ArrayList<>(), // entities to replace
                "",
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
                pokemonSpawnConfig
        );
    }

    private static List<SpawnablePokemonProperties> parsePokemonRoster(ListTag potentialsList) {
        List<SpawnablePokemonProperties> roster = new ArrayList<>();

        for (int i = 0; i < potentialsList.size(); i++) {
            CompoundTag entryTag = potentialsList.getCompound(i);
            int weight = entryTag.contains("weight") ? entryTag.getInt("weight") : 10;

            // Dig down to data -> entity -> Pokemon
            CompoundTag pokemonNbt = entryTag.getCompound("data").getCompound("entity").getCompound("Pokemon");

            // Extract core properties
            String species = pokemonNbt.getString("Species").replace("cobblemon:", "");
            int level = pokemonNbt.contains("Level") ? pokemonNbt.getInt("Level") : 25;
            float scaleModifier = pokemonNbt.contains("ScaleModifier") ? pokemonNbt.getFloat("ScaleModifier") : 1.0f;
            boolean isShiny = pokemonNbt.getBoolean("Shiny");
            String gender = pokemonNbt.getString("Gender");
            String teraType = pokemonNbt.getString("TeraType").replace("cobblemon:", "").toLowerCase(java.util.Locale.ROOT);
            int dynaMaxLevel = pokemonNbt.contains("DmaxLevel") ? pokemonNbt.getInt("DmaxLevel") : 0;

            // Extract Ability
            CompoundTag abilityTag = pokemonNbt.getCompound("Ability");
            String ability = abilityTag.contains("AbilityName") ? abilityTag.getString("AbilityName") : "";

            // Extract HeldItem
            CompoundTag heldItemTag = pokemonNbt.getCompound("HeldItem");
            String heldItem = heldItemTag.contains("id") ? heldItemTag.getString("id") : "";

            // Extract Nature
            String nature = pokemonNbt.getString("Nature").replace("cobblemon:", "").toLowerCase(java.util.Locale.ROOT);

            // Extract PersistentData flags and custom moves
            CompoundTag persistentData = pokemonNbt.getCompound("PersistentData");
            boolean isAggressive = persistentData.getBoolean("is_aggressive");
            boolean isUncatchable = persistentData.getBoolean("is_uncatchable");
            boolean isAlwaysAlpha = persistentData.getBoolean("is_always_alpha");
            boolean mustBeDefeatedInBattle = persistentData.getBoolean("is_invulnerable");

            List<String> moves = new ArrayList<>();
            if (persistentData.contains("custom_moves", Tag.TAG_LIST)) {
                ListTag movesList = persistentData.getList("custom_moves", Tag.TAG_STRING);
                for (int j = 0; j < movesList.size(); j++) {
                    moves.add(movesList.getString(j));
                }
            }

            // Extract EVs and IVs with robust key fallbacks
            CompoundTag evsTag = pokemonNbt.getCompound("EVs");
            List<Integer> defaultEVs = List.of(
                    getStatValue(evsTag, "cobblemon:hp"),
                    getStatValue(evsTag, "cobblemon:attack"),
                    getStatValue(evsTag, "cobblemon:defence"),
                    getStatValue(evsTag, "cobblemon:special_attack"),
                    getStatValue(evsTag, "cobblemon:special_defence"),
                    getStatValue(evsTag, "cobblemon:speed")
            );

            CompoundTag ivsBaseTag = pokemonNbt.getCompound("IVs").getCompound("Base");
            List<Integer> defaultIVs = List.of(
                    getStatValue(ivsBaseTag, "cobblemon:hp"),
                    getStatValue(ivsBaseTag, "cobblemon:attack"),
                    getStatValue(ivsBaseTag, "cobblemon:defence"),
                    getStatValue(ivsBaseTag, "cobblemon:special_attack"),
                    getStatValue(ivsBaseTag, "cobblemon:special_defence"),
                    getStatValue(ivsBaseTag, "cobblemon:speed")
            );

            // Extract Forms & Features
            List<String> forms = new ArrayList<>();
            if (pokemonNbt.contains("Features", Tag.TAG_LIST)) {
                ListTag featuresList = pokemonNbt.getList("Features", Tag.TAG_COMPOUND);
                for (int j = 0; j < featuresList.size(); j++) {
                    CompoundTag featureTag = featuresList.getCompound(j);
                    String featureId = featureTag.getString("cobblemon:feature_id");
                    if (!featureId.isEmpty()) {
                        forms.add(featureId);
                    }
                }
            }
            if (forms.isEmpty() && pokemonNbt.contains("FormId")) {
                String formId = pokemonNbt.getString("FormId");
                if (!formId.isEmpty() && !formId.equalsIgnoreCase("normal")) {
                    forms.add(formId);
                }
            }

            // Extract Aspects
            List<String> aspects = new ArrayList<>();
            if (pokemonNbt.contains("ForcedAspects", Tag.TAG_LIST)) {
                ListTag forcedAspectsList = pokemonNbt.getList("ForcedAspects", Tag.TAG_STRING);
                for (int j = 0; j < forcedAspectsList.size(); j++) {
                    String aspect = forcedAspectsList.getString(j);
                    if (!forms.contains(aspect)) {
                        aspects.add(aspect);
                    }
                }
            }

            // Build Stats
            SpawnablePokemonStats stats = new SpawnablePokemonStats(
                    forms, level, gender, nature, defaultEVs, defaultIVs,
                    ability, moves, heldItem, dynaMaxLevel, teraType, isShiny
            );

            // Build full properties
            SpawnablePokemonProperties properties = new SpawnablePokemonProperties(
                    species, weight, scaleModifier, isUncatchable, mustBeDefeatedInBattle,
                    isAggressive, isAlwaysAlpha, aspects, stats
            );

            roster.add(properties);
        }

        return roster;
    }

    private static int getStatValue(CompoundTag tag, String key) {
        if (tag.contains(key)) {
            return tag.getInt(key);
        }
        String shortKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        if (tag.contains(shortKey)) {
            return tag.getInt(shortKey);
        }
        return 0;
    }

    private static List<SpawnerProperties.PokemonWaveDefinition> parsePokemonWavesRoster(ListTag wavesList) {
        List<SpawnerProperties.PokemonWaveDefinition> waves = new ArrayList<>();

        for (int i = 0; i < wavesList.size(); i++) {
            CompoundTag waveTag = wavesList.getCompound(i);

            // Extract core wave properties (using fallbacks mirroring the codec defaults)
            int baseMobs = waveTag.contains("baseMobs") ? waveTag.getInt("baseMobs") : 4;
            int mobsPerPlayer = waveTag.contains("mobsPerPlayer") ? waveTag.getInt("mobsPerPlayer") : 1;

            // Extract the spawn potentials specifically scoped to this wave
            ListTag spawnPotentials = waveTag.getList("spawnPotentials", Tag.TAG_COMPOUND);

            // Reuse the existing roster parser for the inner list
            List<SpawnablePokemonProperties> pokemonToSpawn = parsePokemonRoster(spawnPotentials);

            waves.add(new SpawnerProperties.PokemonWaveDefinition(baseMobs, mobsPerPlayer, pokemonToSpawn));
        }

        return waves;
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
