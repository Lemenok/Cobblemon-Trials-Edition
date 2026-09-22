package com.lemenok.cobblemontrialsedition.network;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Objects;

import static com.lemenok.cobblemontrialsedition.network.NetworkHelpers.*;

public record SaveSpawnerC2SPacket(BlockPos pos, SpawnerProperties properties) implements CustomPacketPayload {
    public static final Type<SaveSpawnerC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemontrialsedition", "save_spawner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveSpawnerC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                writeProperties(buf, packet.properties());
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                SpawnerProperties properties = readProperties(buf);
                return new SaveSpawnerC2SPacket(pos, properties);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(BlockPos pos, SpawnerProperties properties) {
        if (Minecraft.getInstance().getConnection() != null) {
            // Wrap the payload in a ServerboundCustomPayloadPacket
            Minecraft.getInstance().getConnection().send(
                    new ServerboundCustomPayloadPacket(new SaveSpawnerC2SPacket(pos, properties))
            );
        }
    }

    // --- Serialization Helpers ---

    private static void writeProperties(RegistryFriendlyByteBuf buf, SpawnerProperties props) {
        buf.writeCollection(Objects.requireNonNullElse(props.blockTypesToReplace(), List.of()), (b, loc) -> b.writeResourceLocation(loc));
        buf.writeCollection(Objects.requireNonNullElse(props.mobEntitiesInSpawnerToReplace(), List.of()), (b, loc) -> b.writeResourceLocation(loc));

        buf.writeInt(props.ticksBetweenSpawnAttempts());
        buf.writeInt(props.spawnerCooldown());
        buf.writeInt(props.playerDetectionRange());
        buf.writeInt(props.spawnRange());
        buf.writeInt(props.maximumNumberOfSimultaneousPokemon());
        buf.writeInt(props.maximumNumberOfSimultaneousPokemonAddedPerPlayer());
        buf.writeInt(props.totalNumberOfPokemonPerTrial());
        buf.writeInt(props.totalNumberOfPokemonPerTrialAddedPerPlayer());

        // TODO: Make it so these have to exist.
        //buf.writeCollection(props.lootTables(), (b, loc) -> b.writeResourceLocation(loc));
        //buf.writeCollection(props.ominousLootTables(), (b, loc) -> b.writeResourceLocation(loc));
        SimpleWeightedRandomList<ResourceKey<LootTable>> safeLootTables = Objects.requireNonNullElse(props.lootTables(), SimpleWeightedRandomList.empty());
        var unwrappedLootTables = safeLootTables.unwrap();
        buf.writeVarInt(unwrappedLootTables.size());
        for (var wrapper : unwrappedLootTables) {
            buf.writeResourceKey(wrapper.data());
            buf.writeInt(wrapper.weight().asInt());
        }

        SimpleWeightedRandomList<ResourceKey<LootTable>> safeOminous = Objects.requireNonNullElse(props.ominousLootTables(), SimpleWeightedRandomList.empty());
        var unwrappedOminous = safeOminous.unwrap();
        buf.writeVarInt(unwrappedOminous.size());
        for (var wrapper : unwrappedOminous) {
            buf.writeResourceKey(wrapper.data());
            buf.writeInt(wrapper.weight().asInt());
        }

        buf.writeBoolean(props.ominousSpawnerAttacksEnabled());
        buf.writeBoolean(props.doPokemonSpawnedGlow());

        // TODO: Make it so these have to exist.
        //writePokemonRoster(buf, props.listOfPokemonToSpawn());
        //writePokemonRoster(buf, props.listOfOminousPokemonToSpawn());
        writePokemonRoster(buf, Objects.requireNonNullElse(props.spawns().listOfPokemonToSpawn(), List.of()));
        writePokemonRoster(buf, Objects.requireNonNullElse(props.spawns().listOfOminousPokemonToSpawn(), List.of()));
        writePokemonWaveRoster(buf, props.spawns().waves());
        writePokemonWaveRoster(buf, props.spawns().ominousWaves());
    }
}
