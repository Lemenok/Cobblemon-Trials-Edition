package com.lemenok.cobblemontrialsedition.network;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public record OpenSpawnerConfigS2CPacket(BlockPos pos, SpawnerProperties properties, List<ResourceLocation> availableLootTables) implements CustomPacketPayload {
    public static final Type<OpenSpawnerConfigS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemontrialsedition", "open_spawner_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSpawnerConfigS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                writeProperties(buf, packet.properties());
                buf.writeCollection(packet.availableLootTables(), (b, loc) -> b.writeResourceLocation(loc));
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                SpawnerProperties properties = readProperties(buf);
                List<ResourceLocation> availableLootTables = buf.readList(b -> b.readResourceLocation());
                return new OpenSpawnerConfigS2CPacket(pos, properties, availableLootTables);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

// --- Serialization Helpers ---

    private static void writeProperties(RegistryFriendlyByteBuf buf, SpawnerProperties props) {
        // Use explicit lambdas to avoid type inference errors
        buf.writeCollection(props.blockTypesToReplace(), (b, loc) -> b.writeResourceLocation(loc));
        buf.writeCollection(props.mobEntitiesInSpawnerToReplace(), (b, loc) -> b.writeResourceLocation(loc));

        // Write integers
        buf.writeInt(props.ticksBetweenSpawnAttempts());
        buf.writeInt(props.spawnerCooldown());
        buf.writeInt(props.playerDetectionRange());
        buf.writeInt(props.spawnRange());
        buf.writeInt(props.maximumNumberOfSimultaneousPokemon());
        buf.writeInt(props.maximumNumberOfSimultaneousPokemonAddedPerPlayer());
        buf.writeInt(props.totalNumberOfPokemonPerTrial());
        buf.writeInt(props.totalNumberOfPokemonPerTrialAddedPerPlayer());

        // Write loot tables
        var unwrappedLootTables = props.lootTables().unwrap();
        buf.writeVarInt(unwrappedLootTables.size());
        for (var wrapper : unwrappedLootTables) {
            buf.writeResourceKey(wrapper.data());
            buf.writeInt(wrapper.weight().asInt());
        }

        var unwrappedOminous = props.ominousLootTables().unwrap();
        buf.writeVarInt(unwrappedOminous.size());
        for (var wrapper : unwrappedOminous) {
            buf.writeResourceKey(wrapper.data());
            buf.writeInt(wrapper.weight().asInt());
        }

        // Write booleans
        buf.writeBoolean(props.ominousSpawnerAttacksEnabled());
        buf.writeBoolean(props.doPokemonSpawnedGlow());

        // Write complex nested rosters
        writePokemonRoster(buf, props.listOfPokemonToSpawn());
        writePokemonRoster(buf, props.listOfOminousPokemonToSpawn());
    }

    private static SpawnerProperties readProperties(RegistryFriendlyByteBuf buf) {
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

        return new SpawnerProperties(
                blockTypes, mobEntities, ticks, cooldown, playerRange, spawnRange,
                maxSim, maxSimPlayer, total, totalPlayer,
                lootTables, ominousLootTables, ominousAttacks, glow,
                roster, ominousRoster
        );
    }

    private static void writePokemonRoster(RegistryFriendlyByteBuf buf, List<SpawnablePokemonProperties> roster) {
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

    private static List<SpawnablePokemonProperties> readPokemonRoster(RegistryFriendlyByteBuf buf) {
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
}
