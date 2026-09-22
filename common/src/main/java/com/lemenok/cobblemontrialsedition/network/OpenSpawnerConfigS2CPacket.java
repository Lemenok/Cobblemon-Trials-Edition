package com.lemenok.cobblemontrialsedition.network;

import com.lemenok.cobblemontrialsedition.config.SpawnConfig;
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

import static com.lemenok.cobblemontrialsedition.network.NetworkHelpers.*;

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
        writePokemonRoster(buf, props.spawns().listOfPokemonToSpawn());
        writePokemonRoster(buf, props.spawns().listOfOminousPokemonToSpawn());
        writePokemonWaveRoster(buf, props.spawns().waves());
        writePokemonWaveRoster(buf, props.spawns().ominousWaves());
    }
}
