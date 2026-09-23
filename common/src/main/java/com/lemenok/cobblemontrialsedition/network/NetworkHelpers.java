package com.lemenok.cobblemontrialsedition.network;

import com.lemenok.cobblemontrialsedition.config.SpawnConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class NetworkHelpers {
    public static List<SpawnablePokemonProperties> readPokemonRoster(RegistryFriendlyByteBuf buf) {
        return buf.readList(b -> {
            String species = b.readUtf();
            int weight = b.readInt();
            float scale = b.readFloat();
            boolean uncatchable = b.readBoolean();
            boolean defeat = b.readBoolean();
            boolean aggressive = b.readBoolean();
            boolean alpha = b.readBoolean();
            List<String> aspects = b.readList(b2 -> b2.readUtf());

            SpawnablePokemonStats stats = null;
            if (b.readBoolean()) {
                stats = new SpawnablePokemonStats(
                        b.readList(b2 -> b2.readUtf()), // form
                        b.readInt(), // level
                        b.readUtf(), // gender
                        b.readUtf(), // nature
                        b.readList(b2 -> Integer.valueOf(b2.readUtf())), // default EVs
                        b.readList(b2 -> Integer.valueOf(b2.readUtf())), // default IVs
                        b.readUtf(), // ability
                        b.readList(b2 -> b2.readUtf()), // moves
                        b.readUtf(), // heldItem
                        b.readInt(), // dynaMaxLevel
                        b.readUtf(), // teraType
                        b.readBoolean() // isShiny
                );
            }

            return new SpawnablePokemonProperties(
                    species, weight, scale, uncatchable, defeat,
                    aggressive, alpha, aspects, stats
            );
        });
    }

    public static void writePokemonRoster(RegistryFriendlyByteBuf buf, List<SpawnablePokemonProperties> roster) {
        buf.writeCollection(roster, (b, poke) -> {
            b.writeUtf(poke.species());
            b.writeInt(poke.weight());
            b.writeFloat(poke.scaleModifier());
            b.writeBoolean(poke.isUncatchable());
            b.writeBoolean(poke.mustBeDefeatedInBattle());
            b.writeBoolean(poke.isAggressive());
            b.writeBoolean(poke.isAlwaysAlpha());
            b.writeCollection(poke.aspects(), (b2, aspect) -> b2.writeUtf(aspect));

            SpawnablePokemonStats stats = poke.spawnablePokemonStats();
            b.writeBoolean(stats != null);

            if (stats != null) {
                b.writeCollection(stats.form(), (b2, str) -> b2.writeUtf(str));
                b.writeInt(stats.level());
                b.writeUtf(stats.gender());
                b.writeUtf(stats.nature());
                b.writeCollection(stats.defaultEVs(), (b2, str) -> b2.writeUtf(String.valueOf(str)));
                b.writeCollection(stats.defaultIVs(), (b2, str) -> b2.writeUtf(String.valueOf(str)));
                b.writeUtf(stats.ability());
                b.writeCollection(stats.moves(), (b2, str) -> b2.writeUtf(str));
                b.writeUtf(stats.heldItem());
                b.writeInt(stats.dynaMaxLevel());
                b.writeUtf(stats.teraType());
                b.writeBoolean(stats.isShiny());
            }
        });
    }

    public static SpawnerProperties readProperties(RegistryFriendlyByteBuf buf) {
        List<ResourceLocation> blockTypes = buf.readList(b -> b.readResourceLocation());
        List<ResourceLocation> mobEntities = buf.readList(b -> b.readResourceLocation());

        int ticks = buf.readInt();
        int cooldown = buf.readInt();
        int playerRange = buf.readInt();
        int spawnRange = buf.readInt();
        int maxSim = buf.readInt();
        int maxSimPlayer = buf.readInt();
        int total = buf.readInt();
        int totalPlayer = buf.readInt();

        int lootTablesSize = buf.readVarInt();
        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> lootTablesBuilder = new SimpleWeightedRandomList.Builder<>();
        for (int i = 0; i < lootTablesSize; i++) {
            lootTablesBuilder.add(buf.readResourceKey(net.minecraft.core.registries.Registries.LOOT_TABLE), buf.readInt());
        }
        SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables = lootTablesBuilder.build();

        int ominousLootTablesSize = buf.readVarInt();
        SimpleWeightedRandomList.Builder<ResourceKey<LootTable>> ominousLootTablesBuilder = new SimpleWeightedRandomList.Builder<>();
        for (int i = 0; i < ominousLootTablesSize; i++) {
            ominousLootTablesBuilder.add(buf.readResourceKey(net.minecraft.core.registries.Registries.LOOT_TABLE), buf.readInt());
        }
        SimpleWeightedRandomList<ResourceKey<LootTable>> ominousLootTables = ominousLootTablesBuilder.build();

        boolean ominousAttacks = buf.readBoolean();
        boolean glow = buf.readBoolean();

        List<SpawnablePokemonProperties> roster = readPokemonRoster(buf);
        List<SpawnablePokemonProperties> ominousRoster = readPokemonRoster(buf);
        List<SpawnerProperties.WaveDefinition> wavesRoster = readPokemonWaveRoster(buf);
        List<SpawnerProperties.WaveDefinition> ominousWavesRoster = readPokemonWaveRoster(buf);

        boolean isWaveMode = buf.readBoolean();

        SpawnConfig spawnConfig = new SpawnConfig(isWaveMode, roster, ominousRoster, wavesRoster, ominousWavesRoster);

        return new SpawnerProperties(
                blockTypes, mobEntities, ticks, cooldown, playerRange, spawnRange,
                maxSim, maxSimPlayer, total, totalPlayer,
                lootTables, ominousLootTables, ominousAttacks, glow,
                spawnConfig
        );
    }

    public static List<SpawnerProperties.WaveDefinition> readPokemonWaveRoster(RegistryFriendlyByteBuf buf) {
        return buf.readList(b -> {
            int mobsInWave = b.readInt();
            int mobsInWaveAddedPerPlayer = b.readInt();
            List<SpawnablePokemonProperties> pokemonToSpawn = readPokemonRoster((RegistryFriendlyByteBuf) b);

            return new SpawnerProperties.WaveDefinition(mobsInWave, mobsInWaveAddedPerPlayer, pokemonToSpawn);
        });
    }

    public static void writePokemonWaveRoster(RegistryFriendlyByteBuf buf, List<SpawnerProperties.WaveDefinition> waves) {
        buf.writeCollection(waves, (b, wave) -> {
            b.writeInt(wave.mobsInWave());
            b.writeInt(wave.mobsInWaveAddedPerPlayer());
            writePokemonRoster((RegistryFriendlyByteBuf) b, wave.pokemonToSpawn());
        });
    }
}
